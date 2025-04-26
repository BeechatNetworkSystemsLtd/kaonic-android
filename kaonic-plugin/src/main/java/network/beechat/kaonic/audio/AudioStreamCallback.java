package network.beechat.kaonic.audio;

public interface AudioStreamCallback {
    void onResult(int size, byte[] buffer);
}