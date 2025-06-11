package network.beechat.kaonic.models.calls;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

import network.beechat.kaonic.models.KaonicEventData;

public class CallEventData extends KaonicEventData {
    @JsonProperty("call_id")
    public @NonNull String callId;
    public @NonNull String id;

    public CallEventData() {
        super();
        this.callId = "";
        this.id = "";
    }

    public CallEventData(@NonNull String address, long timestamp, @NonNull String callId) {
        super(address, timestamp);
        this.callId = callId;
        this.id = UUID.randomUUID().toString();
    }

    public CallEventData(@NonNull String address, @NonNull String callId) {
        super(address, System.currentTimeMillis());
        this.callId = callId;
        this.id = UUID.randomUUID().toString();
    }
}
