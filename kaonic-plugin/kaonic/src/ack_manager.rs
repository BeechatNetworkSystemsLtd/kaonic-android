use std::{collections::HashMap, sync::Arc, time::Duration};

use tokio::{
    sync::{
        oneshot::{self, Receiver},
        Mutex,
    },
    time::timeout,
};

pub struct AckManager<T> {
    pending: Arc<Mutex<HashMap<T, oneshot::Sender<()>>>>,
}

impl<T: std::hash::Hash + Eq + Clone> AckManager<T> {
    pub fn new() -> Self {
        Self {
            pending: Arc::new(Mutex::new(HashMap::new())),
        }
    }

    pub async fn wait_for_ack(&self, id: &T) -> Receiver<()> {
        let mut pending = self.pending.lock().await;
        let (tx, rx) = oneshot::channel();
        pending.insert(id.clone(), tx);
        return rx;
    }

    pub async fn handle_ack(&self, id: T) {
        let maybe_sender = {
            let mut pending = self.pending.lock().await;
            pending.remove(&id)
        };

        if let Some(sender) = maybe_sender {
            let _ = sender.send(()); // Ignore error if receiver already dropped (timeout).
        }
    }
}
