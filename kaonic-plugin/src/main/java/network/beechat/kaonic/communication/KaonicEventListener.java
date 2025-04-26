package network.beechat.kaonic.communication;

import androidx.annotation.NonNull;

import network.beechat.kaonic.models.KaonicEvent;

public interface KaonicEventListener {
    void onEventReceived(@NonNull KaonicEvent event);
}
