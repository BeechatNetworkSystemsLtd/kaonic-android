package network.beechat.kaonic.models;


public class KaonicEvent<T extends KaonicEventData> {
    public final String type;
    /// from who that event is
    /// if the event is my( my message, etc) - address is mine
    public final String address;
    public final long timestamp;
    public T data;

    public KaonicEvent(String type, String address, long timestamp) {
        this.type = type;
        this.address = address;
        this.timestamp = timestamp;
    }

    public KaonicEvent(String type, String address, long timestamp, T data) {
        this.type = type;
        this.address = address;
        this.timestamp = timestamp;
        this.data = data;
    }
}

