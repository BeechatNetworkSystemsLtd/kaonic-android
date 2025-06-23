package network.beechat.kaonic.models.calls;

import androidx.annotation.Keep;

@Keep
public class CallAudioData extends CallEventData {
    public final byte[] bytes;

    @Keep
    public CallAudioData() {
        super();
        bytes = new byte[]{};
    }

    @Keep
    public CallAudioData(String address, String callId, byte[] buffer) {
        super(address, callId);
        bytes = buffer;
    }
}
