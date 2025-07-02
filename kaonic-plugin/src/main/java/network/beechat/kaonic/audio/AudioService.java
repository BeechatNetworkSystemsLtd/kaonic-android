package network.beechat.kaonic.audio;

import static android.media.AudioManager.STREAM_MUSIC;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Build;
import android.util.Log;

@SuppressLint("MissingPermission")
public class AudioService {
    private final String TAG = "AndroidAudio";

    private final int SAMPLE_RATE = 8000;
    private final int CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    private final int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private final int SAMPLE_BUFFER_SIZE = 512;

    private final AudioRecord audioRecord;
    private final AudioTrack audioTrack;
    private AcousticEchoCanceler echoCanceler = null;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private final CircularBuffer circularBuffer;
    private Thread recordingThread = null;
    private Thread playingThread = null;
    private AudioStreamCallback audioStreamCallback = null;

    public AudioService() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN, AUDIO_ENCODING);

        int minOutBufferSize = Math.max(AudioTrack.getMinBufferSize(SAMPLE_RATE,CHANNEL_OUT, AudioFormat.ENCODING_PCM_16BIT), SAMPLE_BUFFER_SIZE);

        circularBuffer = new CircularBuffer(minOutBufferSize * 16);

        audioRecord = new AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(new AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setEncoding(AUDIO_ENCODING)
                        .setChannelMask(CHANNEL_IN)
                        .build())
                .setBufferSizeInBytes(minOutBufferSize)
                .build();

        AudioTrack.Builder audioTrackBuilder = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setEncoding(AUDIO_ENCODING)
                        .setChannelMask(CHANNEL_OUT)
                        .build())
                .setBufferSizeInBytes(minOutBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioTrackBuilder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY);
        }

        audioTrack = audioTrackBuilder.build();

        Log.d(TAG, "instance created");
    }

    public void setAudioStreamCallback(AudioStreamCallback audioStreamCallback) {
        this.audioStreamCallback = audioStreamCallback;
    }

    public void startPlaying() {
        if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            Log.e(TAG, "AudioTrack initialization failed");
            return;
        }

        audioTrack.play();
        isPlaying = true;

        playingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioData();
            }
        }, "AudioPlayer Thread");
        playingThread.start();
    }

    public void stopPlaying() {
        if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            audioTrack.stop();
        }

        isPlaying = false;

        if (playingThread != null) {
            try {
                playingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            playingThread = null;
        }
    }

    public void play(byte[] data, int length) {

       circularBuffer.write(data, 0, length);
    }

    public void startRecording() {
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord is not initialized, cannot start recording");
            return;
        }

        audioRecord.startRecording();
        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                readAudioData();
            }
        }, "AudioRecorder Thread");

        recordingThread.start();
    }

    public void stopRecording() {
        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            audioRecord.stop();
        }

        isRecording = false;

        if (recordingThread != null) {
            try {
                recordingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recordingThread = null;
        }
    }

    private void writeAudioData() {
        byte[] audioBuffer = new byte[SAMPLE_BUFFER_SIZE * 3];

        while (isPlaying) {
            if (circularBuffer.hasSufficientData(audioBuffer.length)) {
                int read = circularBuffer.read(audioBuffer, 0, audioBuffer.length);
                if (read > 0) {
                    audioTrack.write(audioBuffer, 0, read);
                }
            }
        }
    }

    private void readAudioData() {
        byte[] audioBuffer = new byte[SAMPLE_BUFFER_SIZE];

        while (isRecording) {
            int read = audioRecord.read(audioBuffer, 0, audioBuffer.length);

            if (read > 0 && audioStreamCallback != null) {
                audioStreamCallback.onResult(read, audioBuffer);
            }
        }
    }
}