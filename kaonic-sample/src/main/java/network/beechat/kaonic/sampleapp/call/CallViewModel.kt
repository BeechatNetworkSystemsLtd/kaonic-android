package network.beechat.kaonic.sampleapp.call

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import network.beechat.kaonic.sampleapp.services.call.CallScreenState
import network.beechat.kaonic.sampleapp.services.call.CallService

class CallViewModelFactory(
    private val address: String,
    private val callService: CallService
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CallViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CallViewModel(address, callService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CallViewModel(
    val address: String, private val callService: CallService,
) : ViewModel() {

    val callState: StateFlow<CallScreenState> = callService.callState

    private val _elapsedTime = MutableStateFlow(0)
    val elapsedTime: StateFlow<Int> = _elapsedTime

    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null
    init {
        scope.launch {
            callService.callState.collect { state ->
                when (state) {
                    CallScreenState.callInProgress -> {
                        startTimer()
                    }
                    CallScreenState.finished -> {
                        stopTimer()
                    }
                    else -> {
                    }
                }
            }
        }
    }

    fun answerCall() {
        callService.answerCall()
    }

    fun rejectCall() {
        callService.rejectCall()
    }

    @SuppressLint("DefaultLocale")
    fun formatElapsedTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    private fun startTimer() {
        timerJob = scope.launch {
            while (isActive) {
                delay(1000)
                _elapsedTime.emit(_elapsedTime.value + 1)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}