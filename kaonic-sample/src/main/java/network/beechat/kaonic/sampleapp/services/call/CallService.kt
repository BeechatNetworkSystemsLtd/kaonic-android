package network.beechat.kaonic.sampleapp.services.call

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import network.beechat.kaonic.audio.AudioService
import network.beechat.kaonic.models.KaonicEventType
import network.beechat.kaonic.models.calls.CallAudioData
import network.beechat.kaonic.models.calls.CallEventData
import network.beechat.kaonic.sampleapp.services.KaonicService
import java.util.UUID


enum class CallScreenState {
    idle,
    incoming,
    outgoing,
    callInProgress,
    finished,
}

class CallService( scope: CoroutineScope) {
    lateinit var audioService: AudioService
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _navigationEvents = MutableSharedFlow<String>()
    val navigationEvents: SharedFlow<String> = _navigationEvents

    private val _callState = MutableStateFlow(CallScreenState.idle)
    val callState: StateFlow<CallScreenState> = _callState

    private var _activeCallId: String? = null
    val activeCallId: String?
        get() = _activeCallId
    private var _activeCallAddress: String? = null
    val activeCallAddress: String?
        get() = _activeCallAddress

    init {
        scope.launch {
            KaonicService.events
                .filter { event -> KaonicEventType.callEvents.contains(event.type) }
                .collect { event ->
                    when (event.type) {
                        KaonicEventType.CALL_INVOKE -> {
                            val callEventData = event.data as CallEventData
                            handleCallInvoke(callEventData.callId, callEventData.address)
                        }

                        KaonicEventType.CALL_ANSWER -> {
                            val callEventData = event.data as CallEventData
                            handleCallAnswer(callEventData.callId, callEventData.address)
                        }

                        KaonicEventType.CALL_REJECT,
                        KaonicEventType.CALL_TIMEOUT -> {
                            val callEventData = event.data as CallEventData
                            handleCallReject(callEventData.callId, callEventData.address)
                        }

                        KaonicEventType.CALL_AUDIO -> {
                            val callAudioData = event.data as CallAudioData
                            audioService.play(callAudioData.bytes, callAudioData.bytes.size)
                        }
                    }
                }
        }

    }

    fun createCall(address: String) {
//        if (_activeCallId != null) return

        _activeCallId = UUID.randomUUID().toString()
        _activeCallAddress = address
        KaonicService.startCall(_activeCallId!!, _activeCallAddress!!)
        scope.launch {
            _callState.emit(CallScreenState.outgoing)
        }
    }

    fun answerCall() {
        if (_activeCallId == null || _activeCallAddress == null) return

        KaonicService.answerCall(_activeCallId!!, _activeCallAddress!!)
        scope.launch {
            _callState.emit(CallScreenState.callInProgress)
        }
    }

    fun rejectCall() {
        if (_activeCallId == null || _activeCallAddress == null) return

        KaonicService.rejectCall(_activeCallId!!, _activeCallAddress!!)
        scope.launch {
            _callState.emit(CallScreenState.finished)
            delay(150)
            _callState.emit(CallScreenState.idle)
        }
        _activeCallId = null
        _activeCallAddress = null
    }

    private fun handleCallInvoke(callId: String, address: String) {
        _activeCallId = callId
        _activeCallAddress = address
        scope.launch {
            _callState.emit(CallScreenState.incoming)
            _navigationEvents.emit("incomingCall/${callId}/${address}")
        }
    }

    private fun handleCallReject(callId: String, address: String) {
        if (callId != _activeCallId) return
        scope.launch {
            _callState.emit(CallScreenState.finished)
            delay(150)
            _callState.emit(CallScreenState.idle)
        }
        _activeCallId = null
        _activeCallAddress = null
    }

    private fun handleCallAnswer(callId: String, address: String) {
        if (callId != _activeCallId) return
        scope.launch {
            _callState.emit(CallScreenState.callInProgress)
        }
    }

}