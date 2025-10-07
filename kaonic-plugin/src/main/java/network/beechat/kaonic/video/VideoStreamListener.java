package network.beechat.kaonic.video;

public interface VideoStreamListener {
    void onFrameReceived(String address, String callId, byte[] data);
}
