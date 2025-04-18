package network.beechat.kaonic.libsource;

import android.content.Context;

import androidx.annotation.NonNull;

public class KaonicLib {

    static {
        System.loadLibrary("kaonic");
    }

    private KaonicDataChannelListener channelListener;

    private static KaonicLib instance;

    private KaonicLib(Context context) {
        // Initialize with context if needed
    }

    public static synchronized KaonicLib getInstance(Context context) {
        if (instance == null) {
            instance = new KaonicLib(context);
        }
        return instance;
    }


    public void setChannelListener(@NonNull KaonicDataChannelListener channelListener) {
        this.channelListener = channelListener;
    }

    public void removeChannelListener() {
        this.channelListener = null;
    }

    public void transmit(String address, byte[] bytes) {

    }

    public void received(String address, byte[] bytes) {
        // callback.received(address, bytes)
    }
}
