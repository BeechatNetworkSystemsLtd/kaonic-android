package network.beechat.kaonic.communication;

import network.beechat.kaonic.models.KaonicEvent;

public interface KaonicEventListener {
    void onEventReceived(KaonicEvent event);
}
