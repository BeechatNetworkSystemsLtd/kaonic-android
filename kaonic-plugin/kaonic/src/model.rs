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
    pub chatId: String,
    pub address: String,
    pub timestamp: i64,
    pub text: String,
}

#[derive(Serialize, Deserialize)]
pub struct MessageAcknowledge {
    pub id: String,
    pub chatId: String,
}

#[derive(Serialize, Deserialize)]
pub struct CallAudioData {
    pub callId: String,
}
