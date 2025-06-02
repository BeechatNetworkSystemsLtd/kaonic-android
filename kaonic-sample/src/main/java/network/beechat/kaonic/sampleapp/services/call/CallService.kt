package network.beechat.kaonic.sampleapp.services.call

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
}

class CallService(private val context: Context, scope: CoroutineScope) {
    lateinit var audioService: AudioService
    private val scope = CoroutineScope(Dispatchers.Main)
    private var ringtone: Ringtone? = null

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

    fun initAudio() {
        audioService = AudioService()
        audioService.setAudioStreamCallback(this::onAudioResult)
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
            stopRingtone()
            startAudio()
        }
    }

    fun rejectCall() {
        if (_activeCallId == null || _activeCallAddress == null) return

        KaonicService.rejectCall(_activeCallId!!, _activeCallAddress!!)
        scope.launch {
            _callState.emit(CallScreenState.idle)
            stopRingtone()
            stopAudio()
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
            _callState.emit(CallScreenState.incoming)
            _navigationEvents.emit("incomingCall/${callId}/${address}")
            playRingtone()
        }
    }

    private fun handleCallReject(callId: String, address: String) {
        if (callId != _activeCallId) return
        scope.launch {
            _callState.emit(CallScreenState.idle)
            stopRingtone()
            stopAudio()
        }
        _activeCallId = null
        _activeCallAddress = null
    }

    private fun handleCallAnswer(callId: String, address: String) {
        if (callId != _activeCallId) return
        scope.launch {
            _callState.emit(CallScreenState.callInProgress)
            stopRingtone()
            startAudio()
        }
    }

    private fun playRingtone() {
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
        ringtone?.play()
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
    }


    private fun onAudioResult(size: Int, buffer: ByteArray) {
        if (_activeCallId == null || _activeCallAddress == null) return

        KaonicService.sendCallAudio(_activeCallId!!, _activeCallAddress!!, buffer)
//        nativeSendAudio(this.pointer, buffer)
    }


    private fun startAudio() {
        audioService.startPlaying()
        audioService.startRecording()
    }

    private fun stopAudio() {
        audioService.stopRecording()
        audioService.stopPlaying()
    }
}