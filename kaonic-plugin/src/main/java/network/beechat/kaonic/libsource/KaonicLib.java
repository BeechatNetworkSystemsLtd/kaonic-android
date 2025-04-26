package network.beechat.kaonic.libsource;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import network.beechat.kaonic.audio.AudioService;
import network.beechat.kaonic.audio.AudioStreamCallback;

public class KaonicLib {
    final private String TAG = "KaonicLib";

    static {
        System.loadLibrary("kaonic");
    }

    private static KaonicLib instance;
    private final AudioService audioService = new AudioService();
    private KaonicDataChannelListener channelListener;

    private KaonicLib(Context context) {
        audioService.setAudioStreamCallback(this::onAudioResult);
        Log.i(TAG, "KaonicLib initialized");
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

    public void playAudio(int size, byte[] buffer){
        Log.i(TAG, "playAudio requested");
        audioService.play(buffer, size);
    }

    private void onAudioResult(int size, byte[] buffer) {
        Log.i(TAG, "onAudioResult");
        //kaonic.feedAudio()
    }
}
