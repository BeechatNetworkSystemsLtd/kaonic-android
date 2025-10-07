package network.beechat.kaonic.sampleapp.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import network.beechat.kaonic.models.KaonicEventType
import network.beechat.kaonic.models.calls.CallAudioData
import network.beechat.kaonic.models.calls.CallEventData
import network.beechat.kaonic.models.video.VideoFrameReceived


class VideoStreamingService {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var _activeCallAddress: String? = null
    val activeCallAddress: String?
        get() = _activeCallAddress
    private var _listening = false

    private val _framesFlow = MutableSharedFlow<ByteArray>()
    val framesFlow: SharedFlow<ByteArray> = _framesFlow

    init {
        scope.launch {
            KaonicService.events
                .filter { event -> event.type == KaonicEventType.VIDEO_FRAME_RECEIVED }
                .collect { event ->
                    if (!_listening || (event.data as VideoFrameReceived).address != _activeCallAddress) return@collect

                    videoFrameReceived((event.data as VideoFrameReceived).buffer)
                }
        }

    }

    fun startListenVideoStreamForAddress(address: String, callId: String) {
        _activeCallAddress = address
        _listening = true
        KaonicService.startVideoStream(callId, address)
    }

    fun stopListenVideoStream() {
        _activeCallAddress = ""
        _listening = false
        KaonicService.stopVideoStream()
    }

    private fun videoFrameReceived(data: ByteArray) {
        scope.launch {
            _framesFlow.emit(data)
        }
    }
}