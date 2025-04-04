package network.beechat.kaonic.audio

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.os.Build
import android.util.Log

@SuppressLint("MissingPermission")
class AudioService {
    private val TAG = "AndroidAudio"

    private val SAMPLE_RATE: Int = 8000
    private val CHANNEL_IN: Int = AudioFormat.CHANNEL_IN_MONO
    private val CHANNEL_OUT: Int = AudioFormat.CHANNEL_OUT_MONO
    private val AUDIO_ENCODING: Int = AudioFormat.ENCODING_PCM_16BIT

    private val audioRecord: AudioRecord
    private val audioTrack: AudioTrack
    private var echoCanceler: AcousticEchoCanceler? = null
    private val bufferSize: Int
    private var isRecording = false
    private var isPlaying = false
    private var circularBuffer: CircularBuffer
    private var recordingThread: Thread? = null
    private var playingThread: Thread? = null
    private var audioStreamCallback: AudioStreamCallback? = null


    init {
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN, AUDIO_ENCODING)

        circularBuffer = CircularBuffer(bufferSize * 16);

        // Configure AudioRecord for capturing audio input
        audioRecord = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AUDIO_ENCODING)
                    .setChannelMask(CHANNEL_IN)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize * 16)
            .build()

        // Configure AudioTrack for playback
        var audioTrackBuilder = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AUDIO_ENCODING)
                    .setChannelMask(CHANNEL_OUT)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize * 32)
            .setTransferMode(AudioTrack.MODE_STREAM)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioTrackBuilder = audioTrackBuilder
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
        }

        audioTrack = audioTrackBuilder
            .build()

        Log.d(TAG, "instance created")
    }

    fun setAudioStreamCallback(audioStreamCallback: AudioStreamCallback?) {
        this.audioStreamCallback = audioStreamCallback
    }

    fun startPlaying() {
        if (audioTrack.state != AudioTrack.STATE_INITIALIZED) {
            Log.e(TAG, "AudioTrack initialization failed")
            return
        }

        audioTrack.play()
        isPlaying = true

        playingThread = Thread({ writeAudioData() }, "AudioPlayer Thread")
        playingThread!!.start()
    }

    fun stopPlaying() {
        if (audioTrack.state == AudioTrack.STATE_INITIALIZED) {
            audioTrack.stop()
        }

        isPlaying = false

        if (playingThread != null) {
            try {
                playingThread!!.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            playingThread = null
        }
    }

    fun play(data: ByteArray?, length: Int) {
        circularBuffer.write(data!!, 0, length);
    }

    fun startRecording() {
        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord is not initialized, cannot start recording")
            return
        }

        audioRecord.startRecording()
        isRecording = true

        recordingThread = Thread({ readAudioData() }, "AudioRecorder Thread")

        recordingThread!!.start()
    }

    fun stopRecording() {
        if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
            audioRecord.stop()
        }

        isRecording = false

        if (recordingThread != null) {
            try {
                recordingThread!!.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            recordingThread = null
        }
    }

    private fun writeAudioData() {
        val audioBuffer = ByteArray(bufferSize * 2)

        while (isPlaying) {
            if (circularBuffer.hasSufficientData(audioBuffer.size)) {
                val read = circularBuffer.read(audioBuffer, 0, audioBuffer.size);
                if (read > 0) {
                    audioTrack.write(audioBuffer, 0, read);
                }
            }
        }
    }

    private fun readAudioData() {
        val audioBuffer = ByteArray(bufferSize)

        while (isRecording) {
            val read = audioRecord.read(audioBuffer, 0, audioBuffer.size)

            if (read > 0) {
                audioStreamCallback?.onResult(read, audioBuffer)
            }
        }
    }

}