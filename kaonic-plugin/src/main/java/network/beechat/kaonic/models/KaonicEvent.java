package network.beechat.kaonic.models;

import androidx.annotation.Keep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Keep
public class KaonicEvent<T extends KaonicEventData> {
    public final String type;
    public T data;

    @Keep
    public KaonicEvent(String type) {
        this.type = type;
    }

    public KaonicEvent(String type, T data) {
        this.type = type;
        this.data = data;
    }
}

