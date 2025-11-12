package network.beechat.kaonic.sampleapp.nodedetails.video

import android.media.MediaCodec
import android.util.Log
import android.view.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import network.beechat.kaonic.sampleapp.services.VideoStreamingService
import network.beechat.kaonic.video.MpegTsDecoder
import network.beechat.kaonic.video.ReceiverPipelineManager
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
    var decoder: MpegTsDecoder? = null
    var decoderPipeline: ReceiverPipelineManager? = null

    init {
        videoStreamingService.startListenVideoStreamForAddress(address, callId)

        // Listen to video frames and decode them
        viewModelScope.launch {
            videoStreamingService.framesFlow.collect { frameData ->
//                decoder?.onPacketReceived(frameData)
                decoderPipeline?.feedBytes(frameData, frameData.size)
            }
        }
    }

    fun startStream(surface: Surface) {

        decoderPipeline = ReceiverPipelineManager(surface)
//        decoder = MpegTsDecoder(surface, 640, 480)
    }

    private fun stopStream() {
//        decoder?.stop()
        decoderPipeline?.stop()
//        videoDecoder.stopDecode()
//        videoStreamingService.stopListenVideoStream()
    }

    override fun onCleared() {
        super.onCleared()
        stopStream()
    }
}