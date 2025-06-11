use std::{sync::Arc, time::Duration};

use rand_core::OsRng;
use reticulum::{
    destination::{link::LinkEvent, DestinationName, SingleInputDestination},
    hash::AddressHash,
    identity::PrivateIdentity,
    iface::InterfaceManager,
    packet::PACKET_MDU,
    transport::{Transport, TransportConfig},
};
use rmp_serde::{Deserializer, Serializer};
use serde::{Deserialize, Serialize};
use tokio::{
    sync::{
        mpsc::{Receiver, Sender},
        Mutex,
    },
    time::timeout,
};
use tokio_util::sync::CancellationToken;

use crate::{
    ack_manager::AckManager,
    cache::CacheSet,
    event::Event,
    model::{
        Acknowledge, AnnounceData, Broadcast, CallAnswer, CallAudioData, CallInvoke, CallReject, ChatCreate,
        Contact, ContactData, FileChunk, FileStart, Message, MessengerError,
    },
};

struct MessengerHandler<T: Platform> {
    identity: PrivateIdentity,
    contact: ContactData,
    transport: Arc<Mutex<Transport>>,
    platform: Arc<Mutex<T>>,
    known_ids: CacheSet<String>,
    ack_manager: AckManager<String>,
}

pub enum MessengerCommand {
    SendMessage(Message),
    CallInvoke(CallInvoke),
    CallAnswer(CallAnswer),
    CallReject(CallReject),
    CallAudioData(CallAudioData),
    SendFileStart(FileStart),
    SendFileChunk(FileChunk),
    Broadcast(Broadcast),
    ChatCreate(ChatCreate),
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
    pub fn new(
        identity: PrivateIdentity,
        contact: ContactData,
        name: impl Into<String>,
        platform: T,
    ) -> Self {
        let transport = Transport::new(TransportConfig::new(
            name,
            &PrivateIdentity::new_from_rand(OsRng),
            false,
        ));

        let (cmd_send, cmd_recv) = tokio::sync::mpsc::channel::<MessengerCommand>(1);

        let handler = MessengerHandler::<T> {
            identity,
            contact,
            transport: Arc::new(Mutex::new(transport)),
            platform: Arc::new(Mutex::new(platform)),
            known_ids: CacheSet::new(512),
            ack_manager: AckManager::new(),
        };

        let handler = Arc::new(Mutex::new(handler));
        let cancel = CancellationToken::new();

        tokio::spawn(handle_messenger(handler.clone(), cancel.clone(), cmd_recv));

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

    pub fn destination_name() -> DestinationName {
        DestinationName::new("kaonic", "messenger.contact")
    }

    pub async fn send(&self, command: MessengerCommand) {
        let _ = self.cmd_send.send(command).await;
    }
}

pub trait Platform {
    fn send_event(&mut self, event: &Event);
    fn feed_audio(&mut self, address: &String, call_id: &String, audio_data: &[u8]);
    fn request_file_chunk(&mut self, address: &String, file_id: &String, chunk_size: usize);
    fn receive_file_chunk(&mut self, address: &String, file_id: &String, data: &[u8]);
    fn receive_broadcast(&mut self, address: &String, id: &String, topic: &String, data: &[u8]);
}

fn serialize_internal_event(buf: &mut Vec<u8>, event: &Event) -> Result<(), MessengerError> {
    let result = event.serialize(&mut Serializer::new(buf));
    if let Err(_) = result {
        return Err(MessengerError::SerdeError);
    }

    Ok(())
}

impl<T: Platform> MessengerHandler<T> {

    /// Send event into output links connected to destination with specified address
    async fn send_out(&self, address: &AddressHash, event: &Event) {
        let mut buf = Vec::new();

        if let Ok(_) = serialize_internal_event(&mut buf, event) {
            self.transport
                .lock()
                .await
                .send_to_out_links(address, &buf)
                .await;
        }
    }

    /// Send event into all output links
    async fn send_out_all(&self, event: &Event) {
        let mut buf = Vec::new();

        if let Ok(_) = serialize_internal_event(&mut buf, event) {
            self.transport
                .lock()
                .await
                .send_to_all_out_links(&buf)
                .await;
        }
    }

    async fn send_ack(&self, address: &AddressHash, ack: Acknowledge) {
        self.send_out(address, &Event::Acknowledge(ack)).await;
    }
}

/// Entry point for messenger async handler's
async fn handle_messenger<T: Platform + Send + 'static>(
    handler: Arc<Mutex<MessengerHandler<T>>>,
    cancel: CancellationToken,
    cmd_recv: Receiver<MessengerCommand>,
) {
    let transport = handler.lock().await.transport.clone();

    let contact_destination = {
        let identity = handler.lock().await.identity.clone();

        transport
            .lock()
            .await
            .add_destination(identity, Messenger::<T>::destination_name())
            .await
    };

    log::info!(
        "messenger: contact destination is {}",
        contact_destination.lock().await.desc.address_hash
    );

    let _ = tokio::join!(
        handle_announces(handler.clone(), cancel.clone()),
        handle_advertise(handler.clone(), cancel.clone(), contact_destination.clone()),
        handle_in_data(handler.clone(), cancel.clone(), contact_destination.clone(),),
        handle_out_data(handler.clone(), cancel.clone()),
        handle_commands(
            handler.clone(),
            cancel.clone(),
            contact_destination.clone(),
            cmd_recv
        ),
    );
}

/// Periodically sends announces for contact destination
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
            Ok(announce) = announces.recv() => {

                let announce_data = Deserialize::deserialize(&mut Deserializer::new(announce.app_data.as_slice()));

                if let Ok(announce_data) = announce_data {
                    let announce_data: AnnounceData = announce_data;
                    let destination = announce.destination.lock().await;

                    let transport = handler.lock().await.transport.clone();
                    let link = transport.lock().await.link(destination.desc).await;

                    log::trace!("messenger: announce contact '{}'={} link={}", announce_data.contact.name, destination.desc.address_hash, link.lock().await.id());

                    let contact = Contact {
                        address: destination.desc.address_hash.to_hex_string(),
                        contact: announce_data.contact,
                    };

                    let platform = handler.lock().await.platform.clone();

                    platform.lock().await.send_event(&Event::ContactFound(contact));
                }
            }
        }
    }
}

/// Send's event to destination and wait for acknowledge
async fn send_ack_event<T: Platform>(
    event_id: &String,
    event: Event,
    address: &AddressHash,
    handler: Arc<Mutex<MessengerHandler<T>>>,
) -> Result<(), MessengerError> {
    const MAX_REPEATS: usize = 8;

    let mut result = Err(MessengerError::Timeout);

    for repeat in 0..MAX_REPEATS {
        let rx = {
            let handler = handler.lock().await;
            handler.send_out(&address, &event).await;
            handler.ack_manager.wait_for_ack(&event_id).await
        };

        // TODO: change to backoff random timeout
        match timeout(Duration::from_millis(888), rx).await {
            Ok(_) => {
                result = Ok(());
                break;
            }
            Err(_) => {
                log::warn!("messenger: message({}) = {} nack", event_id, repeat)
            }
        }
    }

    result
}

/// Manages commands from platform client
async fn handle_commands<T: Platform + Send + 'static>(
    handler: Arc<Mutex<MessengerHandler<T>>>,
    cancel: CancellationToken,
    contact_destination: Arc<Mutex<SingleInputDestination>>,
    mut cmd_recv: Receiver<MessengerCommand>,
) {
    let contact_address = contact_destination
        .lock()
        .await
        .desc
        .address_hash
        .to_hex_string();

    loop {
        tokio::select! {
            _ = cancel.cancelled() => {
                break;
            },
            Some(cmd) = cmd_recv.recv() => {
                match cmd {
                    MessengerCommand::CallAudioData(mut call) => {
                        let address_str = call.address.clone();
                        let address = AddressHash::new_from_hex_string(&address_str).unwrap();

                        call.address = contact_address.clone();

                        handler.lock().await.send_out(&address, &Event::CallAudioData(call)).await;
                    },
                    MessengerCommand::CallInvoke(mut call) => {
                        let address_str = call.address.clone();
                        let call_id = call.call_id.clone();
                        let address = AddressHash::new_from_hex_string(&address_str).unwrap();

                        log::info!("messenger: call invoke addr:{} call-id:{}", address, call_id);

                        call.address = contact_address.clone();

                        let _ = send_ack_event(&call.id.clone(), Event::CallInvoke(call), &address, handler.clone()).await;
                    },
                    MessengerCommand::CallAnswer(mut call) => {
                        let address_str = call.address.clone();
                        let call_id = call.call_id.clone();
                        let address = AddressHash::new_from_hex_string(&address_str).unwrap();

                        log::info!("messenger: call answer addr:{} call-id:{}", address, call_id);

                        call.address = contact_address.clone();

                        let _ = send_ack_event(&call.id.clone(), Event::CallAnswer(call), &address, handler.clone()).await;
                    },
                    MessengerCommand::Broadcast(mut broadcast) => {
                        broadcast.address = contact_address.clone();
                        handler.lock().await.send_out_all(&Event::Broadcast(broadcast)).await;
                    },
                    MessengerCommand::CallReject(mut call) => {
                        let address_str = call.address.clone();
                        let call_id = call.call_id.clone();
                        let address = AddressHash::new_from_hex_string(&address_str).unwrap();

                        log::info!("messenger: call reject addr:{} call-id:{}", address, call_id);

                        call.address = contact_address.clone();

                        let _ = send_ack_event(&call.id.clone(), Event::CallReject(call), &address, handler.clone()).await;
                    },
                    MessengerCommand::SendFileStart(mut file) => {
                        let address_str = file.address.clone();
                        let file_id = file.file_id.clone();
                        let address = AddressHash::new_from_hex_string(&address_str).unwrap();

                        log::debug!("messenger: send file {}({}) {}kBytes to {}", file.file_name, file.file_id, file.file_size / 1024, address);

                        file.address = contact_address.clone();

                        let result = send_ack_event(&file.id.clone(), Event::FileStart(file), &address, handler.clone()).await;
                        if let Ok(_) = result {
                            let platform = handler.lock().await.platform.clone();
                            tokio::spawn(async move {
                                platform.lock().await.request_file_chunk(&address_str, &file_id, PACKET_MDU / 2);
                            });
                        }
                    },
                    MessengerCommand::SendFileChunk(mut file) => {

                        let address_str = file.address.clone();
                        let file_id = file.file_id.clone();
                        let address = AddressHash::new_from_hex_string(&address_str).unwrap();

                        log::trace!("messenger: send file chunk {}  to {}", file.file_id, address);

                        file.address = contact_address.clone();

                        let result = send_ack_event(&file.id.clone(), Event::FileChunk(file), &address, handler.clone()).await;
                        if let Ok(_) = result {
                            let platform = handler.lock().await.platform.clone();
                            tokio::spawn(async move {
                                platform.lock().await.request_file_chunk(&address_str, &file_id, PACKET_MDU / 4);
                            });
                        }
                    },
                    MessengerCommand::SendMessage(mut message) => {

                        let address = AddressHash::new_from_hex_string(&message.address).unwrap();
                        log::debug!("messenger: send message to {}", address);

                        message.address = contact_address.clone();

                        let _ = send_ack_event(&message.id.clone(), Event::Message(message), &address, handler.clone()).await;
                    },
                    MessengerCommand::ChatCreate(mut chat) => {

                        let address = AddressHash::new_from_hex_string(&chat.address).unwrap();
                        log::debug!("messenger: create chat with {}", address);

                        chat.address = contact_address.clone();

                        let _ = send_ack_event(&chat.chat_id.clone(), Event::ChatCreate(chat), &address, handler.clone()).await;
                    },
                }
            },
        }
    }
}

async fn handle_advertise<T: Platform + Send + 'static>(
    handler: Arc<Mutex<MessengerHandler<T>>>,
    cancel: CancellationToken,
    contact_destination: Arc<Mutex<SingleInputDestination>>,
) {
    let transport = handler.lock().await.transport.clone();

    let mut announce_data_buf = Vec::new();

    let announce_data = AnnounceData {
        contact: handler.lock().await.contact.clone(),
    };

    let _ = announce_data.serialize(&mut Serializer::new(&mut announce_data_buf));

    const ANNOUNCE_INTERVAL: Duration = Duration::from_secs(5);

    loop {
        transport
            .lock()
            .await
            .send_announce(&contact_destination, Some(&announce_data_buf))
            .await;

        tokio::select! {
            _ = tokio::time::sleep(ANNOUNCE_INTERVAL) => { },
            _ = cancel.cancelled() => {
                break;
            },
        }
    }
}

async fn handle_ack_event<T: Platform + Send + 'static>(
    handler: &mut MessengerHandler<T>,
    event: Event,
) {
    let id = event.to_id();
    let ack_kind = event.to_ack_kind();

    let from_address = event.address_hash();
    log::trace!("messenger: receive event from {}", from_address);

    if handler.known_ids.insert(&id) {
        match event {
            Event::ChatCreate(_) | Event::Message(_) | Event::FileStart(_) => {
                handler.platform.lock().await.send_event(&event);
            }
            Event::CallInvoke(_) | Event::CallAnswer(_) | Event::CallReject(_) => {
                handler.platform.lock().await.send_event(&event);
            }
            Event::FileChunk(chunk) => {
                handler.platform.lock().await.receive_file_chunk(
                    &chunk.address,
                    &chunk.file_id,
                    &chunk.data,
                );
            }
            _ => {}
        }
    } else {
        log::warn!("messenger: duplicate '{}' detected", id);
    }

    let ack = Acknowledge::new(id, ack_kind);

    // TODO: check if it is needed
    handler.ack_manager.handle_ack(&ack.id).await;

    handler.send_ack(&from_address, ack).await;
}

// This function handles request events from a client via "input" link
async fn handle_in_data<T: Platform + Send + 'static>(
    handler: Arc<Mutex<MessengerHandler<T>>>,
    cancel: CancellationToken,
    contact_destination: Arc<Mutex<SingleInputDestination>>,
) {
    let _contact_address = contact_destination.lock().await.desc.address_hash;

    let transport = handler.lock().await.transport.clone();
    let mut link_events = transport.lock().await.in_link_events();
    loop {
        tokio::select! {
            Ok(link_event) = link_events.recv() => {
                match link_event.event {
                    LinkEvent::Data(data) => {
                        let event = Deserialize::deserialize(&mut Deserializer::new(data.as_slice()));

                        if let Ok(event) = event {
                            match event {
                                Event::CallAudioData(call) => {
                                    handler.lock().await.platform.lock().await.feed_audio(&call.address, &call.call_id, &call.data[..]);
                                },
                                Event::ChatCreate(_) | Event::Message(_) |
                                Event::FileStart(_) | Event::FileChunk(_) |
                                Event::CallInvoke(_) | Event::CallAnswer(_) | Event::CallReject(_) => {
                                    let mut handler = handler.lock().await;
                                    handle_ack_event(&mut handler, event).await;
                                },
                                Event::Acknowledge(ack) => {
                                    handler.lock().await.ack_manager.handle_ack(&ack.id).await;
                                },
                                Event::ContactFound(_) => {},
                                Event::ContactConnect(_) => {},
                                Event::Broadcast(broadcast) => {
                                    let mut handler = handler.lock().await;
                                    if handler.known_ids.insert(&broadcast.id) {
                                        handler.platform.lock().await.receive_broadcast(
                                            &broadcast.address,
                                            &broadcast.id,
                                            &broadcast.topic,
                                            &broadcast.data);
                                    }
                                },
                            }
                        } else if let Err(err) = event {
                            log::error!("messenger: invalid out event {}", err);
                        }
                    },
                    LinkEvent::Activated => {},
                    LinkEvent::Closed => {},
                }
            },
            _ = cancel.cancelled() => {
                break;
            },
        }
    }
}

// This function handles response events from client via "output" link
async fn handle_out_data<T: Platform + Send + 'static>(
    handler: Arc<Mutex<MessengerHandler<T>>>,
    cancel: CancellationToken,
) {
    let transport = handler.lock().await.transport.clone();
    let mut link_events = transport.lock().await.out_link_events();
    loop {
        tokio::select! {
            Ok(link_event) = link_events.recv() => {
                match link_event.event {
                    LinkEvent::Data(data)=> {
                        let event = Deserialize::deserialize(&mut Deserializer::new(data.as_slice()));
                        if let Ok(event) = event {
                            match event {
                                // TODO: remove
                                Event::Acknowledge(ack) => {
                                    handler.lock().await.ack_manager.handle_ack(&ack.id).await;
                                },
                                _ => { },
                            }
                        } else if let Err(err) = event {
                            log::error!("messenger: invalid in event {}", err);
                        }
                    },
                    LinkEvent::Activated => {},
                    LinkEvent::Closed => {},
                }
            },
            _ = cancel.cancelled() => {
                break;
            },
        }
    }
}
