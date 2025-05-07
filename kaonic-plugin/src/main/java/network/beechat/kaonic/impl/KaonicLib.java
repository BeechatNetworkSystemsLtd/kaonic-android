package network.beechat.kaonic.impl;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Objects;

import network.beechat.kaonic.audio.AudioService;
import network.beechat.kaonic.storage.SecureStorageHelper;

public class KaonicLib {
    final private String TAG = "KaonicLib";

    static {
        System.loadLibrary("kaonic");
        libraryInit();
    }

    public interface EventListener {
        void onEventReceived(String jsonData);
        void onFileChunkRequest(String fileId);
        void onFileChunkReceived(String fileId, byte[] bytes);
    }

    private static KaonicLib instance;
    private final AudioService audioService = new AudioService();
    private final SecureStorageHelper secureStorageHelper;

    private final long pointer;
    private EventListener eventListener;

    private KaonicLib(Context context) throws Exception {

        pointer = this.nativeInit(context);

        secureStorageHelper = new SecureStorageHelper(context);
        audioService.setAudioStreamCallback(this::onAudioResult);

        Log.i(TAG, "KaonicLib initialized");

        start(loadSecret());
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

    public void start(String secret) {
        if (secret != null) {
            nativeStart(this.pointer, secret);
        }
    }

    public void sendFileChunk(String fileId, byte[] data)
    {
        // native
    }

    public void transmit(String eventJson) {
        if (eventJson != null) {
            nativeTransmit(this.pointer, eventJson);
        }
    }

    public String generate() {
       String json = nativeGenerate(this.pointer);
       // TODO: Parse JSON
       return json;
    }

    private native void nativeTransmit(long ptr, String eventJson);

    private native void nativeStart(long ptr, String privateKey);
    private native void nativeStop(long ptr);

    private native long nativeInit(Context context);
    private native void nativeDestroy(long ptr);
    private native void nativeSendAudio(long ptr, byte[] data);

    private native String nativeGenerate(long ptr);

    private static native void libraryInit();

    private void receive(String json) {
        Log.i(TAG, "kaonicDataReceived");
        if (eventListener != null) {
             eventListener.onEventReceived(json);
        }
    }

    public void startAudio() {
        Log.i(TAG, "startAudio requested");
        audioService.startPlaying();
        audioService.startRecording();
    }

    public void stopAudio() {
        Log.i(TAG, "stopAudio requested");
        audioService.stopRecording();
        audioService.stopPlaying();
    }

    public void feedAudio(byte[] buffer) {
        Log.i(TAG, "playAudio requested");
        audioService.play(buffer, buffer.length);
    }

    private void onAudioResult(int size, byte[] buffer) {
        Log.i(TAG, "onAudioResult");
        nativeSendAudio(this.pointer, buffer);
    }

    private String loadSecret() {
        Log.i(TAG, "checkPrivateKey");
        String secret = null;
        try {
            String SECRET_TAG = "KAONIC_SECRET";
            secret = secureStorageHelper.get(SECRET_TAG);
            if (secret == null) {
                secret = generate();
                secureStorageHelper.put(SECRET_TAG, secret);
                Log.i(TAG, "private key stored");
            }

        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return secret;
    }
}
