package network.beechat.kaonic.sampleapp.services.call

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import network.beechat.kaonic.models.KaonicEventType
import network.beechat.kaonic.models.calls.CallEventData
import network.beechat.kaonic.sampleapp.services.KaonicService
import androidx.core.net.toUri
import java.util.UUID


enum class CallScreenState {
    idle,
    incoming,
    outgoing,
    callInProgress,
}

class CallService(private val context: Context, scope: CoroutineScope) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var mediaPlayer: MediaPlayer? = null

    private val _navigationEvents = MutableSharedFlow<String>()
    val navigationEvents: SharedFlow<String> = _navigationEvents

    private val _callState = MutableStateFlow(CallScreenState.idle)
    val callState: StateFlow<CallScreenState> = _callState

    private var _activeCallId: String? = null
    val activeCallId: String? = _activeCallId
    private var _activeCallAddress: String? = null
    val activeCallAddress: String? = _activeCallAddress

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
                    }
                }
        }
    }

    fun createCall(address: String) {
//        if (_activeCallId != null) return

        _activeCallId = UUID.randomUUID().toString()
        _activeCallAddress = address
        KaonicService.startCall(_activeCallId!!,_activeCallAddress!!)
        scope.launch {
            _callState.emit(CallScreenState.outgoing)
        }
    }

    fun answerCall() {
        if (_activeCallId == null || _activeCallAddress == null) return

        KaonicService.answerCall(_activeCallId!!, _activeCallAddress!!)
        scope.launch {
            _callState.emit(CallScreenState.callInProgress)
            stopRingtone()
        }
    }

    fun rejectCall() {
        if (_activeCallId == null || _activeCallAddress == null) return

        KaonicService.rejectCall(_activeCallId!!, _activeCallAddress!!)
        scope.launch {
            _callState.emit(CallScreenState.idle)
            stopRingtone()
        }
    }

    private fun handleCallInvoke(callId: String, address: String) {
        if (_activeCallId != null) {
            KaonicService.rejectCall(callId, address)
            return
        }

        _activeCallId = callId
        _activeCallAddress = address
        scope.launch {
            _navigationEvents.emit("incomingCall/${callId}/${address}")
            playRingtone()
        }
    }

    private fun handleCallReject(callId: String, address: String) {
        if (callId != _activeCallId) return
        scope.launch {
            _callState.emit(CallScreenState.idle)
            stopRingtone()
        }
        _activeCallId = null
        _activeCallAddress = null
    }

    private fun handleCallAnswer(callId: String, address: String) {
        if (callId != _activeCallId) return
        scope.launch {
            _callState.emit(CallScreenState.callInProgress)
            stopRingtone()
        }
    }

    private fun playRingtone() {
        val ringtoneUri = "android.resource://${context.packageName}/raw/ringtone".toUri()
        mediaPlayer = MediaPlayer.create(context, ringtoneUri)
        if (mediaPlayer != null) {
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } else {
            throw IllegalStateException("MediaPlayer initialization failed")
        }
    }

    private fun stopRingtone() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}