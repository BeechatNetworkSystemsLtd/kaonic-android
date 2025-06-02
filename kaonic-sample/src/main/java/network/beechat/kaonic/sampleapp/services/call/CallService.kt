package network.beechat.kaonic.sampleapp.services.call

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import network.beechat.kaonic.models.KaonicEventType
import network.beechat.kaonic.models.calls.CallEventData
import network.beechat.kaonic.sampleapp.services.KaonicService

class CallService(scope: CoroutineScope) {

    private val _navigationEvents = MutableSharedFlow<String>()
    val navigationEvents: SharedFlow<String> = _navigationEvents

    init {
        scope.launch {
            KaonicService.events
                .filter { event -> KaonicEventType.callEvents.contains(event.type) }
                .collect { event ->
                    when (event.type) {
                        KaonicEventType.CALL_INVOKE -> {
                            val callEventData = event.data as CallEventData
                            _navigationEvents.emit("callScreen/${callEventData.callId}/${callEventData.address}")
                            handleCallInvoke(callEventData.callId, callEventData.address)
                        }
                    }
                }
        }
    }

    private fun handleCallInvoke(callId: String, address: String) {
        // play ringtone
    }

    private fun handleCallReject(callId: String, address: String) {
        // stop play  ringtone
        // cancel call
    }

    private fun handleCallAnswer(callId: String, address: String) {
        // Handle call answer logic
    }
}