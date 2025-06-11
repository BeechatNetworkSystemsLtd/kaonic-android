package network.beechat.kaonic.models.calls;

public class CallAudioData extends CallEventData {
    public final byte[] bytes;

    public CallAudioData() {
        super();
        bytes = new byte[]{};
    }

    public CallAudioData(String address, String callId, byte[] buffer) {
        super(address, callId);
        bytes = buffer;
    }
}
