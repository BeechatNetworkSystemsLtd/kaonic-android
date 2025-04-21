package network.beechat.kaonic.libsource;

public interface KaonicDataChannelListener {
    void onDataReceive(byte[] bytes, String address);
}
