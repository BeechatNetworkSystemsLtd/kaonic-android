use reticulum::hash::AddressHash;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
pub struct ContactData {
    pub name: String,
}

#[derive(Serialize, Deserialize)]
pub struct Contact {
    pub address: String,
    pub data: ContactData,
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
pub struct Acknowledge {
    pub id: String,
}

impl Acknowledge {
    pub fn new_from_id(id: impl Into<String>) -> Self {
        Self { id: id.into() }
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
