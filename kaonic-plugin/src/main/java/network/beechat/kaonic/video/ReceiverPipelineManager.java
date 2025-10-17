package network.beechat.kaonic.video;

import android.view.Surface;

public class ReceiverPipelineManager {
    static {
        System.loadLibrary("video-receiver");
    }

    private long nativeHandle = 0;

    public ReceiverPipelineManager(Surface surface) {
        nativeHandle = nativeInit(surface);
    }

    public void feedBytes(byte[] data, int length) {
        if (nativeHandle != 0) {
            nativePush(nativeHandle, data, length);
        }
    }

    public void stop() {
        if (nativeHandle != 0) {
            nativeStop(nativeHandle);
            nativeHandle = 0;
        }
    }

    // JNI bindings
    private native long nativeInit(Surface surface);
    private native void nativePush(long handle, byte[] data, int length);
    private native void nativeStop(long handle);
}