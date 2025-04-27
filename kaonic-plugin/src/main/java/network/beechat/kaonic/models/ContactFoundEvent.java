package network.beechat.kaonic.models;

public class ContactFoundEvent extends KaonicEventData {
    protected ContactFoundEvent(String address, long timestamp) {
        super(address, timestamp);
    }

    protected ContactFoundEvent() {
        super("", 0);
    }
}
