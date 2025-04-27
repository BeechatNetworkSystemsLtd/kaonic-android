package network.beechat.kaonic.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KaonicEvent<T extends KaonicEventData> {
    public final String type;
    public T data;

    public KaonicEvent(String type) {
        this.type = type;
    }

    public KaonicEvent(String type, T data) {
        this.type = type;
        this.data = data;
    }
}

