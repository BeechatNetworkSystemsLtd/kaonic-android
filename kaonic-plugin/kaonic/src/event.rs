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

    pub fn address_hash(&self) -> AddressHash {
        match self {
            Event::ContactFound(contact) => AddressHash::new_from_hex_string(&contact.address),
            Event::Message(message) => AddressHash::new_from_hex_string(&message.address),
            Event::CallAudioData(audio_data) => {
                AddressHash::new_from_hex_string(&audio_data.address)
            }
            Event::FileStart(file_start) => AddressHash::new_from_hex_string(&file_start.address),
            Event::FileChunk(file_chunk) => AddressHash::new_from_hex_string(&file_chunk.address),
            Event::ContactConnect(connect) => AddressHash::new_from_hex_string(&connect.address),
            Event::ChatCreate(chat) => AddressHash::new_from_hex_string(&chat.address),
            Event::Acknowledge(_) => Ok(AddressHash::new_empty()),
        }
        .unwrap_or(AddressHash::new_empty())
    }
}
