package network.beechat.kaonic.models;

import androidx.annotation.Keep;

@Keep
public class ContactFoundEvent extends KaonicEventData {
    @Keep
    protected ContactFoundEvent(String address, long timestamp) {
        super(address, timestamp);
    }

    @Keep
    protected ContactFoundEvent() {
        super("", 0);
    }
}
