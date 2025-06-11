package network.beechat.kaonic.communication;

import android.media.Ringtone;
import android.util.Log;

import java.util.Objects;
import java.util.UUID;

import network.beechat.kaonic.audio.AudioService;
import network.beechat.kaonic.audio.AudioStreamCallback;
import network.beechat.kaonic.models.KaonicEvent;
import network.beechat.kaonic.models.KaonicEventData;
import network.beechat.kaonic.models.KaonicEventType;
import network.beechat.kaonic.models.calls.CallEventData;

public class CallHandler {

    private String TAG = "KaonicCallHandler";
    private AudioService audioService;
    private Ringtone ringtone;
    private String activeCallId;
    private String activeAddress;

    void initHandler(AudioStreamCallback audioStreamCallback,
                     Ringtone ringtone) {
        audioService = new AudioService();
        audioService.setAudioStreamCallback(audioStreamCallback);
        this.ringtone = ringtone;
    }

    String getActiveCallId() {
        return activeCallId;
    }

    String getActiveCallAddress() {
        return activeAddress;
    }

    void onCallEventReceived(KaonicEvent<KaonicEventData> event) {
        if (!(event.data instanceof CallEventData)) return;
        switch (event.type) {
            case KaonicEventType.CALL_INVOKE:
                onCallInvoke(event.data.address, ((CallEventData) event.data).callId);
                break;
            case KaonicEventType.CALL_ANSWER:
                onCallAnswer(
                        event.data.address, ((CallEventData) event.data).callId
                );
                break;
            case KaonicEventType.CALL_REJECT:
            case KaonicEventType.CALL_TIMEOUT:
                onCallReject(event.data.address, ((CallEventData) event.data).callId);
                break;
        }
    }


    void play(byte[] bytes,int size){
        audioService.play(bytes, size);
    }

    private void onCallInvoke(String address, String callId) {
        activeCallId = callId;
        activeAddress = address;
        playRingtone();
    }

    private void onCallReject(String address, String callId) {
        if (!Objects.equals(activeCallId, callId)) {
            Log.e(TAG, "Reject is impossible - incorrect callId");
        }

        stopRingtone();
        stopAudio();
        activeCallId = null;
        activeAddress = null;
    }

    private void onCallAnswer(String address, String callId) {
        if (!Objects.equals(activeCallId, callId)) {
            Log.e(TAG, "Answer is impossible - incorrect callId");
        }

        activeCallId = callId;
        activeAddress=address;
        stopRingtone();
        startAudio();
    }

    private void playRingtone() {
        if (ringtone != null) {
            ringtone.play();
        }
    }

    private void stopRingtone() {
        if (ringtone != null) {
            ringtone.stop();
        }
    }

    private void startAudio() {
        audioService.startPlaying();
        audioService.startRecording();
    }

    private void stopAudio() {
        audioService.stopRecording();
        audioService.stopPlaying();
    }
}
