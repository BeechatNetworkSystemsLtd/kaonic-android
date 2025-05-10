use reticulum::hash::AddressHash;
use serde::{Deserialize, Serialize};

use crate::model::{
    Acknowledge, AcknowledgeKind, CallAudioData, ChatCreate, Contact, ContactConnect, FileChunk,
    FileStart, Message,
};

#[derive(Serialize, Deserialize)]
#[serde(tag = "type", content = "data")]
pub enum Event {
    ContactFound(Contact),
    Message(Message),
    Acknowledge(Acknowledge),
    CallAudioData(CallAudioData),
    FileStart(FileStart),
    FileChunk(FileChunk),
    ContactConnect(ContactConnect),
    ChatCreate(ChatCreate),
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
            Event::ContactConnect(connect) => connect.address.clone(),
            Event::ChatCreate(chat) => chat.chat_id.clone(),
        }
    }

    pub fn to_ack_kind(&self) -> AcknowledgeKind {
        match self {
            Event::Message(_) => AcknowledgeKind::Message,
            Event::ChatCreate(_) => AcknowledgeKind::Chat,
            Event::FileStart(_) => AcknowledgeKind::FileStart,
            Event::FileChunk(_) => AcknowledgeKind::FileChunk,
            Event::ContactFound(_) => AcknowledgeKind::Generic,
            Event::CallAudioData(_) => AcknowledgeKind::Generic,
            Event::ContactConnect(_) => AcknowledgeKind::Generic,
            Event::Acknowledge(acknowledge) => acknowledge.kind,
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
            Event::ContactConnect(connect) => {
                connect.address = address;
            }
            Event::ChatCreate(chat) => {
                chat.address = address;
            }
            Event::Acknowledge(_) => {}
        }
    }
}
