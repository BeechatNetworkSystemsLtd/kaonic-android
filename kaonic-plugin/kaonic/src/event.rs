use serde::{Deserialize, Serialize};

use crate::model::{CallAudioData, Contact, Message, Acknowledge};

#[derive(Serialize, Deserialize)]
#[serde(tag = "type", content = "data")]
pub enum Event {
    ContactFound(Contact),
    Message(Message),
    Acknowledge(Acknowledge),
    CallAudioData(CallAudioData),
}
