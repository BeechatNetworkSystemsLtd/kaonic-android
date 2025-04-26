package network.beechat.kaonic.libsource;

import android.content.Context;

import androidx.annotation.NonNull;

import network.beechat.kaonic.audio.AudioService;
import network.beechat.kaonic.audio.AudioStreamCallback;

public class KaonicLib {

    static {
        System.loadLibrary("kaonic");
    }

    private static KaonicLib instance;
    private final AudioService audioService = new AudioService();
    private KaonicDataChannelListener channelListener;

    private KaonicLib(Context context) {
        audioService.setAudioStreamCallback(this::onAudioResult);
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

    public void transmit(String dataJson) {

    }

    public void kaonicDataReceived(String dataJson) {
        if (channelListener != null) {
            channelListener.onDataReceive(dataJson);
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

    public void playAudio(int size, byte[] buffer){
        audioService.play(buffer, size);
    }

    private void onAudioResult(int size, byte[] buffer) {
        //kaonic.feedAudio()
    }
}
