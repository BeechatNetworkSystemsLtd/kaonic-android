use serde::{Deserialize, Serialize};

use crate::model::{CallAudioData, Contact, Message, MessageAcknowledge};

#[derive(Serialize, Deserialize)]
#[serde(tag = "type", content = "data")]
pub enum Event {
    ContactFound(Contact),
    Message(Message),
    MessageAcknowledge(MessageAcknowledge),
    CallAudioData(CallAudioData),
}
