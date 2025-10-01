package network.beechat.kaonic.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;


@Keep
public class KaonicLib {
    final private String TAG = "KaonicLib";

    // Load and initialize native Kaonic library
    static {
        System.loadLibrary("kaonic");
        libraryInit();
    }

    public interface EventListener {
        void onEventReceived(@NonNull String jsonData);

        void onFileChunkRequest(@NonNull String fileId, int chunkSize);
        void onFileChunkReceived(@NonNull String fileId, @NonNull byte[] bytes);

        void onBroadcastReceived(@NonNull String address, @NonNull String id,
                                 @NonNull String topic, @NonNull byte[] bytes);

        void onAudioChunkReceived(String address, String callId, byte[] buffer);

        void onVideoChunkReceived(String address, String callId, byte[] buffer);
    }

    private static KaonicLib instance;

    private final long pointer;
    private EventListener eventListener;

    private KaonicLib(Context context) throws Exception {

        pointer = this.nativeInit(context);

        Log.i(TAG, "KaonicLib initialized");
    }

    public static synchronized KaonicLib getInstance(Context context) throws Exception {
        if (instance == null) {
            instance = new KaonicLib(context);
        }
        return instance;
    }

    public void setEventListener(@NonNull EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void removeChannelListener() {
        this.eventListener = null;
    }

    public void start(String secret, String startConfig) {
        if (secret != null) {
            nativeStart(this.pointer, secret, startConfig);
        }
    }

    public void stop() {
        nativeStop(this.pointer);
    }

    public void sendMessage(String eventJson) {
        if (eventJson != null) {
            nativeSendEvent(this.pointer, eventJson);
        }
    }

    public void createChat(String eventJson) {
        if (eventJson != null) {
            nativeSendEvent(this.pointer, eventJson);
        }
    }

    public void sendFile(String eventJson) {
        if (eventJson != null) {
            nativeSendEvent(this.pointer, eventJson);
        }
    }

    public void sendBroadcast(String id, String topic, byte[] data) {
        if (id != null && topic != null && data != null) {
            nativeSendBroadcast(this.pointer, id, topic, data);
        }
    }

    public String generateSecret() {
        return nativeGenerate(this.pointer);
    }

    public String getPresets() {
        return nativeGetPresets(this.pointer);
    }

    public void sendConfig(String configJson) {
        nativeConfigure(this.pointer, configJson);
    }

    public void sendCallEvent(String eventJson) {
        if (eventJson != null) {
            nativeSendEvent(this.pointer, eventJson);
        }
    }

    public void sendCallAudio(String address, String callId, byte[] data) {
            nativeSendAudio(this.pointer, address,callId,data);
    }

    public void sendCallVideo(String address, String callId, byte[] data) {
            nativeSendVideo(this.pointer, address,callId,data);
    }

    private static native void libraryInit();

    private native long nativeInit(Context context);

    private native void nativeDestroy(long ptr);

    private native void nativeStart(long ptr, String secret, String contact);

    private native void nativeStop(long ptr);

    private native String nativeGenerate(long ptr);

    private native String nativeGetPresets(long ptr);

    private native void nativeConfigure(long ptr, String configJson);

    private native void nativeSendEvent(long ptr, String eventJson);

    private native void nativeSendAudio(long ptr, String address, String callId, byte[] data);

    private native void nativeSendVideo(long ptr, String address, String callId, byte[] data);

    private native void nativeSendFileChunk(long ptr, String address, String id, byte[] data);

    private native void nativeSendBroadcast(long ptr, String id, String topic, byte[] data);

    @Keep
    private void receive(String json) {
        if (eventListener != null && json != null) {
            eventListener.onEventReceived(json);
        }
    }

    @Keep
    public void feedAudio(String address, String callId, byte[] buffer) {
        eventListener.onAudioChunkReceived(address, callId, buffer);
    }

    @Keep
    public void feedVideo(String address, String callId, byte[] buffer) {
        eventListener.onVideoChunkReceived(address, callId, buffer);
    }

    /**
     * Send file data chunk to specified address and file id
     */
    @Keep
    public void sendFileChunk(String address, String fileId, byte[] data) {
        nativeSendFileChunk(this.pointer, address, fileId, data);
    }

    @Keep
    private void requestFileChunk(String address, String fileId, int chunkSize) {
        if (eventListener != null) {
            // NOTE: onFileChunkRequest should be invoked from different thread
            // TODO: Optimize thread usage
            new Handler(Looper.getMainLooper()).post(() -> {
                eventListener.onFileChunkRequest(fileId, chunkSize);
            });
        }
    }

    @Keep
    private void receiveFileChunk(String address, String fileId, byte[] data) {
        if (eventListener != null) {
            // NOTE: onFileChunkReceived should be invoked from different thread
            // TODO: Optimize thread usage
            new Handler(Looper.getMainLooper()).post(() -> {
                eventListener.onFileChunkReceived(fileId, data);
            });
        }
    }

    @Keep
    private void receiveBroadcast(String address, String id, String topic, byte[] data) {
        if (eventListener != null) {
            // NOTE: onBroadcastReceived should be invoked from different thread
            // TODO: Optimize thread usage
            new Handler(Looper.getMainLooper()).post(() -> {
                eventListener.onBroadcastReceived(address, id, topic, data);
            });
        }
    }

}
