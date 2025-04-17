package network.beechat.kaonic.audio;

public abstract class AudioStreamCallback {
    public abstract void onResult(int size, byte[] buffer);
}