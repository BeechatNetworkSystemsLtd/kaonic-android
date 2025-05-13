package network.beechat.kaonic.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Objects;

import network.beechat.kaonic.audio.AudioService;
import network.beechat.kaonic.storage.SecureStorageHelper;

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
    private final SecureStorageHelper secureStorageHelper;

    private final long pointer;
    private EventListener eventListener;

    private KaonicLib(Context context) throws Exception {

        pointer = this.nativeInit(context);

        secureStorageHelper = new SecureStorageHelper(context);
        audioService.setAudioStreamCallback(this::onAudioResult);

        Log.i(TAG, "KaonicLib initialized");

        // TODO: move start from communication service
        start(loadSecret(), 
            "{" + 
                "\"contact\":{\"name\":\"Kaonic\"}," +
                "\"connections\":[ {\"type\":\"TcpClient\", \"info\": { \"address\": \"192.168.0.224:4242\"}}]" +
           "}");
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
            nativeSendMessage(this.pointer, eventJson);
        }
    }

    public void createChat(String eventJson) {
        if (eventJson != null) {
            nativeCreateChat(this.pointer, eventJson);
        }
    }

    public void sendFile(String eventJson) {
        if (eventJson != null) {
            nativeSendFile(this.pointer, eventJson);
        }
    }

    public String generate() {
        String json = nativeGenerate(this.pointer);
        // TODO: Parse JSON
        return json;
    }

    private static native void libraryInit();

    private native long nativeInit(Context context);
    private native void nativeDestroy(long ptr);
    private native void nativeStart(long ptr, String secret, String contact);
    private native void nativeStop(long ptr);

    private native String nativeGenerate(long ptr);

    private native void nativeSendMessage(long ptr, String eventJson);
    private native void nativeCreateChat(long ptr, String eventJson);
    private native void nativeSendFile(long ptr, String eventJson);
    private native void nativeSendAudio(long ptr, byte[] data);
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

    public void feedAudio(byte[] buffer) {
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
            new Handler(Looper.getMainLooper()).post(() -> {
                eventListener.onFileChunkRequest(fileId, chunkSize);
            });
        }
    }

    private void receiveFileChunk(String address, String fileId, byte[] data) {
        if (eventListener != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                eventListener.onFileChunkReceived(fileId, data);
            });
        }
    }

    private void onAudioResult(int size, byte[] buffer) {
        Log.i(TAG, "onAudioResult");
        nativeSendAudio(this.pointer, buffer);
    }

    // TODO: Remove secret management from this class
    private String loadSecret() {
        String secret = null;
        try {
            String SECRET_TAG = "KAONIC_SECRET";
            secret = secureStorageHelper.get(SECRET_TAG);
            if (secret == null) {
                secret = generate();
                secureStorageHelper.put(SECRET_TAG, secret);
            }

        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return secret;
    }
}
