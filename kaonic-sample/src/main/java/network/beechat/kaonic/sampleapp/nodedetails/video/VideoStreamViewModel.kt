package network.beechat.kaonic.sampleapp.nodedetails.video

import android.media.MediaCodec
import android.view.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import network.beechat.kaonic.sampleapp.nodedetails.NodeDetailsViewModel
import network.beechat.kaonic.sampleapp.services.ChatService
import network.beechat.kaonic.sampleapp.services.VideoStreamingService
import network.beechat.kaonic.video.VideoStreamDecoder

class VideoStreamViewModelFactory(
    private val address: String,
    private val callId: String,
    private val videoStreamingService: VideoStreamingService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VideoStreamViewModel(address, callId, videoStreamingService) as T
    }
}

class VideoStreamViewModel(
    private val address: String,
    private val callId: String,
    private val videoStreamingService: VideoStreamingService
) : ViewModel() {
    private val videoDecoder = VideoStreamDecoder()

    init {
        videoStreamingService.startListenVideoStreamForAddress(address, callId)

        // Listen to video frames and decode them
        viewModelScope.launch {
            videoStreamingService.framesFlow.collect { frameData ->
                val bufferInfo = MediaCodec.BufferInfo().apply {
                    offset = 0
                    size = frameData.size
                    presentationTimeUs = System.nanoTime() / 1000 // Convert to microseconds
                    flags = 0 // You might need to set appropriate flags based on frame type
                }
                videoDecoder.decodeFrame(frameData)
            }
        }
    }

    fun startStream(surface: Surface) {
        videoDecoder.startDecode(surface)
    }

    private fun stopStream() {
        videoDecoder.stopDecode()
        videoStreamingService.stopListenVideoStream()
    }

    override fun onCleared() {
        super.onCleared()
        stopStream()
    }
}