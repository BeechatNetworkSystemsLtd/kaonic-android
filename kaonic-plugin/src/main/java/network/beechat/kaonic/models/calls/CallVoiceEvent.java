package network.beechat.kaonic.models.calls;

import network.beechat.kaonic.models.KaonicEventData;

public class CallVoiceEvent extends KaonicEventData {
    public final byte[] bytes;

    public CallVoiceEvent(byte[] bytes) {
        this.bytes = bytes;
    }
}
