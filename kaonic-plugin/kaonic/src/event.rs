use reticulum::hash::AddressHash;
use serde::{Deserialize, Serialize};

use crate::model::{Acknowledge, CallAudioData, Contact, FileChunk, FileStart, Message};

#[derive(Serialize, Deserialize)]
#[serde(tag = "type", content = "data")]
pub enum Event {
    ContactFound(Contact),
    Message(Message),
    Acknowledge(Acknowledge),
    CallAudioData(CallAudioData),
    FileStart(FileStart),
    FileChunk(FileChunk),
}

impl Event {
    pub fn to_id(&self) -> String {
        match self {
            Event::ContactFound(contact) => contact.address.clone(),
            Event::Message(message) => message.id.clone(),
            Event::Acknowledge(acknowledge) => acknowledge.id.clone(),
            Event::CallAudioData(call_audio_data) => call_audio_data.call_id.clone(),
            Event::FileStart(file_start) => file_start.id.clone(),
            Event::FileChunk(file_chunk) => file_chunk.id.clone(),
        }
    }

    pub fn change_address(&mut self, address: &AddressHash) {
        let address = address.to_hex_string();
        match self {
            Event::ContactFound(contact) => {
                contact.address = address;
            }
            Event::Message(message) => {
                message.address = address;
            }
            Event::CallAudioData(call_audio_data) => {
                call_audio_data.address = address;
            }
            Event::FileStart(file_start) => {
                file_start.address = address;
            }
            Event::FileChunk(file_chunk) => {
                file_chunk.address = address;
            }
            Event::Acknowledge(_) => {}
        }
    }
}
