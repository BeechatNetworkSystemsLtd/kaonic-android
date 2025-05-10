use serde::{Deserialize, Serialize};

#[derive(Clone, Serialize, Deserialize)]
pub struct ContactData {
    pub name: String,
}

#[derive(Serialize, Deserialize)]
pub struct AnnounceData {
    pub contact: ContactData,
}

#[derive(Serialize, Deserialize)]
pub struct Contact {
    pub address: String,
    pub contact: ContactData,
}

#[derive(Clone, Copy, Serialize, Deserialize)]
pub enum MessengerError {
    Timeout,
    NotFound,
}

#[derive(Serialize, Deserialize)]
pub struct Message {
    pub id: String,
    pub chat_id: String,
    pub address: String,
    pub timestamp: i64,
    pub text: String,
}

#[derive(Serialize, Deserialize)]
pub struct ContactConnect {
    pub address: String,
}

#[derive(Clone, Copy, Serialize, Deserialize)]
pub enum AcknowledgeKind {
    Generic,
    FileStart,
    FileChunk,
    Message,
}

#[derive(Serialize, Deserialize)]
pub struct Acknowledge {
    pub kind: AcknowledgeKind,
    pub id: String,
}

impl Acknowledge {
    pub fn new(id: impl Into<String>, kind: AcknowledgeKind) -> Self {
        Self {
            id: id.into(),
            kind,
        }
    }
}

#[derive(Serialize, Deserialize)]
pub struct CallAudioData {
    pub address: String,
    pub call_id: String,
    pub data: String,
}

#[derive(Serialize, Deserialize)]
pub struct CallStart {
    pub address: String,
    pub call_id: String,
}

#[derive(Serialize, Deserialize)]
pub struct FileStart {
    pub address: String,
    pub id: String,
    pub file_id: String,
    pub chat_id: String,
    pub file_size: usize,
    pub file_name: String,
}

#[derive(Serialize, Deserialize)]
pub struct FileChunk {
    pub address: String,
    pub id: String,
    pub file_id: String,
    pub chat_id: String,
    pub data: Vec<u8>,
}

#[derive(Serialize, Deserialize)]
pub struct KaonicConfig {}

#[derive(Serialize, Deserialize)]
pub struct ConnectionInfo {
    pub address: String,
}

#[derive(Serialize, Deserialize)]
#[serde(tag = "type", content = "info")]
pub enum Connection {
    TcpClient(ConnectionInfo),
    KaonicClient(ConnectionInfo),
}
