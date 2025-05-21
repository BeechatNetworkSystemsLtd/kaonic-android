package network.beechat.kaonic.models.calls;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import network.beechat.kaonic.models.KaonicEventData;

public class CallEventData extends KaonicEventData {
    @JsonProperty("call_id")
    public @NonNull String callId;

    public CallEventData() {
        super();
        this.callId = "";
    }

    public CallEventData(@NonNull String address, long timestamp, @NonNull String callId) {
        super(address, timestamp);
        this.callId = callId;
    }

    public CallEventData(@NonNull String address, @NonNull String callId) {
        super(address, System.currentTimeMillis());
        this.callId = callId;
    }
}
