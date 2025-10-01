package network.beechat.kaonic.models.video;

import androidx.annotation.Keep;

import com.fasterxml.jackson.annotation.JsonProperty;

import network.beechat.kaonic.models.KaonicEventData;

@Keep
public class VideoFrameReceived extends KaonicEventData {
    @JsonProperty("chat_id")
    public final String callId;
    @JsonProperty("buffer")
    public final byte[] buffer;

    @Keep
    public VideoFrameReceived(String address, String callId, byte[] buffer) {
        super(address,System.currentTimeMillis());
        this.callId = callId;
        this.buffer =buffer;

    }
}
