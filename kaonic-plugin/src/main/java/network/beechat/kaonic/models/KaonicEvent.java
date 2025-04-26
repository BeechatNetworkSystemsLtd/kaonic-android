package network.beechat.kaonic.models;


public class KaonicEvent<T extends KaonicEventData> {
    public final String type;
    public final String address;
    public final long timestamp;
    public T data;

    public KaonicEvent(String type, String address, long timestamp) {
        this.type = type;
        this.address = address;
        this.timestamp = timestamp;
    }
}

