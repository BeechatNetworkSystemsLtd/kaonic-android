package network.beechat.kaonic.models;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class KaonicEventData {
    /// from who that event is
    /// if the event is my( my message, etc) - address is mine
    public final @NonNull String address;
    public final long timestamp;

    protected KaonicEventData(@NonNull String address, long timestamp) {
        this.address = address;
        this.timestamp = timestamp;
    }

    public KaonicEventData() {
        this.address = "";
        this.timestamp = 0;
    }
}
