use kaonic::{event::Event, model::Contact, Messenger, Platform};
use reticulum::{
    identity::PrivateIdentity,
    iface::{tcp_client::TcpClient, tcp_server::TcpServer},
    transport::{Transport, TransportConfig},
};

pub struct MockPlatform {}

impl Platform for MockPlatform {
    fn send_event(&mut self, event: &Event) {
        let json = serde_json::to_string_pretty(&event).unwrap();
        println!("Serialized:\n{}", json);
    }
}

async fn create_messenger(name: &str, addr: &str) -> Messenger<MockPlatform> {
    let id = PrivateIdentity::new_from_name(name);

    let messenger = Messenger::new(id, name, MockPlatform {});

    messenger
        .iface_manager()
        .await
        .lock()
        .await
        .spawn(TcpClient::new(addr), TcpClient::spawn);

    return messenger;
}

#[tokio::main]
async fn main() {
    env_logger::Builder::from_env(env_logger::Env::default().default_filter_or("trace")).init();

    let _server_transport = {
        let transport = Transport::new(TransportConfig::new(
            "server",
            &PrivateIdentity::new_from_name("server"),
            true,
        ));

        let iface_manager = transport.iface_manager();
        iface_manager.lock().await.spawn(
            TcpServer::new("0.0.0.0:4242", iface_manager.clone()),
            TcpServer::spawn,
        );
        transport
    };

    let messenger_a = create_messenger("msg-a", "127.0.0.1:4242").await;
    let messenger_b = create_messenger("msg-b", "127.0.0.1:4242").await;

    let _ = tokio::signal::ctrl_c().await;
}
