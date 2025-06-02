package network.beechat.kaonic.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Objects;

import network.beechat.kaonic.audio.AudioService;


public class KaonicLib {
    final private String TAG = "KaonicLib";

    // Load and initialize native Kaonic library
    static {
        System.loadLibrary("kaonic");
        libraryInit();
    }

    public interface EventListener {
        void onEventReceived(String jsonData);

        void onFileChunkRequest(String fileId, int chunkSize);

        void onFileChunkReceived(String fileId, byte[] bytes);
    }

    private static KaonicLib instance;
    private final AudioService audioService = new AudioService();

    private final long pointer;
    private EventListener eventListener;

    private KaonicLib(Context context) throws Exception {

        pointer = this.nativeInit(context);
        audioService.setAudioStreamCallback(this::onAudioResult);

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

    public String generateSecret() {
        String json = nativeGenerate(this.pointer);
        return json;
    }

    public void sendConfig(String configJson){
        nativeConfigure(this.pointer, configJson);
    }

    public void sendCallInvoke(String eventJson) {
        if (eventJson != null) {
            nativeSendEvent(this.pointer, eventJson);
        }
    }

    public void sendCallAnswer(String eventJson) {
        if (eventJson != null) {
            nativeSendEvent(this.pointer, eventJson);
            startAudio();
        }
    }

    public void sendCallReject(String eventJson) {
        if (eventJson != null) {
            nativeSendEvent(this.pointer, eventJson);
            stopAudio();
        }
    }

    private static native void libraryInit();

    private native long nativeInit(Context context);
    private native void nativeDestroy(long ptr);
    private native void nativeStart(long ptr, String secret, String contact);
    private native void nativeStop(long ptr);

    private native String nativeGenerate(long ptr);

    private native void nativeConfigure(long ptr, String configJson);
    private native void nativeSendEvent(long ptr, String eventJson);
    private native void nativeSendAudio(long ptr, String address, String callId, byte[] data);
    private native void nativeSendFileChunk(long ptr, String address, String id, byte[] data);

    private void receive(String json) {
        if (eventListener != null && json != null) {
            eventListener.onEventReceived(json);
        }
    }

    public void startAudio() {
        audioService.startPlaying();
        audioService.startRecording();
    }

    public void stopAudio() {
        audioService.stopRecording();
        audioService.stopPlaying();
    }

    public void feedAudio(String address, String callId, byte[] buffer) {
        audioService.play(buffer, buffer.length);
    }

    /**
     * Send file data chunk to specified address and file id
     */
    public void sendFileChunk(String address, String fileId, byte[] data) {
        nativeSendFileChunk(this.pointer, address, fileId, data);
    }

    private void requestFileChunk(String address, String fileId, int chunkSize) {
        if (eventListener != null) {
            // NOTE: onFileChunkRequest should be invoked from different thread
            // TODO: Optimize thread usage
            new Handler(Looper.getMainLooper()).post(() -> {
                eventListener.onFileChunkRequest(fileId, chunkSize);
            });
        }
    }

    private void receiveFileChunk(String address, String fileId, byte[] data) {
        if (eventListener != null) {
            // NOTE: onFileChunkReceived should be invoked from different thread
            // TODO: Optimize thread usage
            new Handler(Looper.getMainLooper()).post(() -> {
                eventListener.onFileChunkReceived(fileId, data);
            });
        }
    }

    private void onAudioResult(int size, byte[] buffer) {
        // nativeSendAudio(this.pointer, buffer);
    }

}
