use std::{sync::Arc, time::Duration};

use ack_manager::AckManager;
use cache::CacheSet;
use event::Event;

use model::{Contact, ContactData, Message, MessageAcknowledge};
use rand_core::OsRng;
use reticulum::{
    destination::{link::LinkEvent, DestinationName},
    hash::AddressHash,
    identity::PrivateIdentity,
    iface::InterfaceManager,
    transport::{Transport, TransportConfig},
};
use tokio::{
    sync::{
        mpsc::{Receiver, Sender},
        Mutex,
    },
    time::timeout,
};
use tokio_util::sync::CancellationToken;

mod ack_manager;
pub mod cache;
pub mod event;
pub mod jni;
pub mod model;

struct MessengerHandler<T: Platform> {
    id: PrivateIdentity,
    transport: Arc<Mutex<Transport>>,
    platform: Arc<Mutex<T>>,
    known_ids: CacheSet<String>,
    ack_manager: AckManager<String>,
}

pub enum MessengerCommand {
    SendMessage(Message),
    // StartCall(AddressHash),
}

pub struct Messenger<T: Platform> {
    handler: Arc<Mutex<MessengerHandler<T>>>,
    cmd_send: Sender<MessengerCommand>,
    cancel: CancellationToken,
}

impl<T: Platform> Drop for Messenger<T> {
    fn drop(&mut self) {
        self.cancel.cancel();
    }
}

impl<T: Platform + Send + 'static> Messenger<T> {
    pub fn new(id: PrivateIdentity, name: impl Into<String>, platform: T) -> Self {
        let transport = Transport::new(TransportConfig::new(
            name,
            &PrivateIdentity::new_from_rand(OsRng),
            false,
        ));

        let (cmd_send, cmd_recv) = tokio::sync::mpsc::channel::<MessengerCommand>(1);

        let handler = MessengerHandler::<T> {
            id,
            transport: Arc::new(Mutex::new(transport)),
            platform: Arc::new(Mutex::new(platform)),
            known_ids: CacheSet::new(100),
            ack_manager: AckManager::new(),
        };

        let handler = Arc::new(Mutex::new(handler));

        let cancel = CancellationToken::new();
        tokio::spawn(handle_messenger(handler.clone(), cmd_recv, cancel.clone()));

        Self {
            handler,
            cancel,
            cmd_send,
        }
    }

    pub async fn iface_manager(&self) -> Arc<Mutex<InterfaceManager>> {
        return self
            .handler
            .lock()
            .await
            .transport
            .lock()
            .await
            .iface_manager()
            .clone();
    }

    pub async fn send(&self, command: MessengerCommand) {
        let _ = self.cmd_send.send(command).await;
    }
}

pub trait Platform {
    fn send_event(&mut self, event: &Event);
}

impl<T: Platform> MessengerHandler<T> {
    async fn send(&self, address: &AddressHash, event: &Event) {
        let event_json = serde_json::to_string_pretty(&event);

        if let Err(_) = event_json {
            return;
        }

        let event_json = event_json.unwrap();

        self.transport
            .lock()
            .await
            .send_to_in_links(address, event_json.as_bytes())
            .await;
    }
}

async fn handle_messenger<T: Platform + Send + 'static>(
    handler: Arc<Mutex<MessengerHandler<T>>>,
    cmd_recv: Receiver<MessengerCommand>,
    cancel: CancellationToken,
) {
    let _ = tokio::join!(
        handle_announces(handler.clone(), cancel.clone()),
        handle_advertise(handler.clone(), cancel.clone()),
        handle_data(handler.clone(), cancel.clone()),
        handle_commands(handler.clone(), cancel.clone(), cmd_recv),
    );
}

async fn handle_announces<T: Platform + Send + 'static>(
    handler: Arc<Mutex<MessengerHandler<T>>>,
    cancel: CancellationToken,
) {
    let mut announces = {
        handler
            .lock()
            .await
            .transport
            .lock()
            .await
            .recv_announces()
            .await
    };

    loop {
        tokio::select! {
            _ = cancel.cancelled() => {
                break;
            },
            Ok(destination) = announces.recv() => {
                let destination = destination.lock().await;
                // TODO: check if destination is compatible

                let transport = handler.lock().await.transport.clone();

                transport.lock().await.link(destination.desc).await;

                let contact = Contact {
                    address: destination.desc.address_hash.to_hex_string(),
                    data: ContactData {
                        name: "contact".into(),
                    },
                };

                handler.lock().await.platform.lock().await.send_event(&Event::ContactFound(contact));
            }
        }
    }
}

async fn handle_commands<T: Platform>(
    handler: Arc<Mutex<MessengerHandler<T>>>,
    cancel: CancellationToken,
    mut cmd_recv: Receiver<MessengerCommand>,
) {
    loop {
        tokio::select! {
            _ = cancel.cancelled() => {
                break;
            },

            Some(cmd) = cmd_recv.recv() => {
                match cmd {
                    MessengerCommand::SendMessage(message) => {
                        let ack_id = message.id.clone();
                        let event = Event::Message(message);
                        for _repeat in 0..6 {
                            let rx = {
                                let handler = handler.lock().await;
                                handler.send(&AddressHash::new_empty(), &event).await;
                                handler.ack_manager.wait_for_ack(&ack_id).await
                            };

                            match timeout(Duration::from_secs(1), rx).await {
                                Ok(_) => break,
                                Err(_) => {},
                            }
                        }
                    },
                }
            },
        }
    }
}

async fn handle_advertise<T: Platform>(
    handler: Arc<Mutex<MessengerHandler<T>>>,
    cancel: CancellationToken,
) {
    let transport = handler.lock().await.transport.clone();

    let contact_destination = {
        let id = handler.lock().await.id.clone();

        transport
            .lock()
            .await
            .add_destination(id, DestinationName::new("kaonic", "messenger.contact"))
            .await
    };

    loop {
        transport
            .lock()
            .await
            .send_announce(&contact_destination, None)
            .await;

        tokio::select! {
            _ = cancel.cancelled() => {
                break;
            },

            _ = tokio::time::sleep(Duration::from_secs(5)) => {

            },
        }
    }
}

async fn handle_message_data<T: Platform + Send + 'static>(
    handler: &mut MessengerHandler<T>,
    from_address: &AddressHash,
    mut message: Message,
) {
    let ack = MessageAcknowledge {
        id: message.id.clone(),
        chatId: message.chatId.clone(),
    };

    message.address = from_address.to_hex_string();

    if handler.known_ids.insert(&message.id) {
        handler
            .platform
            .lock()
            .await
            .send_event(&Event::Message(message));
    }

    handler.ack_manager.handle_ack(ack.id.clone()).await;

    handler
        .send(from_address, &Event::MessageAcknowledge(ack))
        .await;
}

async fn handle_data<T: Platform + Send + 'static>(
    handler: Arc<Mutex<MessengerHandler<T>>>,
    cancel: CancellationToken,
) {
    let transport = handler.lock().await.transport.clone();
    let mut in_link_events = transport.lock().await.in_link_events();
    loop {
        tokio::select! {
            Ok(link_event) = in_link_events.recv() => {
                match link_event.event {
                    LinkEvent::Data(data)=> {
                        if let Ok(event) = serde_json::from_slice::<Event>(data.as_slice()) {
                            match event {
                                Event::ContactFound(_) => {},
                                Event::MessageAcknowledge(_) => {},
                                Event::Message(message) => {
                                    let mut handler = handler.lock().await;
                                    handle_message_data(&mut handler, &link_event.address_hash, message).await;
                                },
                            }
                        }
                    },
                    LinkEvent::Activated => {
                    },
                    LinkEvent::Closed => {
                    },
                }
            },
            _ = cancel.cancelled() => {
                break;
            },
        }
    }
}
