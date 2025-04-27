package network.beechat.kaonic.libsource;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Objects;

import network.beechat.kaonic.audio.AudioService;
import network.beechat.kaonic.storage.SecureStorageHelper;

public class KaonicLib {
    final private String TAG = "KaonicLib";

    static {
//        System.loadLibrary("kaonic");
    }

    private static KaonicLib instance;
    private final AudioService audioService = new AudioService();
    private final SecureStorageHelper secureStorageHelper;

    private KaonicDataChannelListener channelListener;

    private KaonicLib(Context context) throws Exception {
        secureStorageHelper = new SecureStorageHelper(context);
        audioService.setAudioStreamCallback(this::onAudioResult);
        checkPrivateKey();
        Log.i(TAG, "KaonicLib initialized");
    }

    public static synchronized KaonicLib getInstance(Context context) throws Exception {
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

    public void transmit(String dataJson) {

    }

    public void kaonicDataReceived(String dataJson) {
        Log.i(TAG, "kaonicDataReceived");
        if (channelListener != null) {
            channelListener.onDataReceive(dataJson);
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

    public void playAudio(int size, byte[] buffer) {
        Log.i(TAG, "playAudio requested");
        audioService.play(buffer, size);
    }

    private void onAudioResult(int size, byte[] buffer) {
        Log.i(TAG, "onAudioResult");
        //kaonic.feedAudio()
    }

    private void checkPrivateKey() {
        Log.i(TAG, "checkPrivateKey");
        try {
            String PRIVATE_KEY_TAG = "KAONIC_PRIVATE_KEY";
            String key = secureStorageHelper.get(PRIVATE_KEY_TAG);
            if (key == null) {
                Log.i(TAG, "private key is null. Request kaonic to generate");
                String privateKey = ""; /// kaonic.generateKey()
                Log.i(TAG, "private key generated");
                secureStorageHelper.put(PRIVATE_KEY_TAG, privateKey);
                Log.i(TAG, "private key stored");
            }
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }
}
