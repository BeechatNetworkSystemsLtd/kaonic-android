package network.beechat.kaonic.sampleapp.services.call

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import network.beechat.kaonic.models.KaonicEvent
import network.beechat.kaonic.models.KaonicEventData
import network.beechat.kaonic.models.KaonicEventType
import network.beechat.kaonic.models.messages.MessageTextEvent
import network.beechat.kaonic.sampleapp.services.KaonicService

class CallService(scope: CoroutineScope) {

    init {
        scope.launch {
            KaonicService.events
                .filter { event -> KaonicEventType.callEvents.contains(event.type) }
                .collect { event ->
                    when (event.data) {
                        is MessageTextEvent -> handleTextMessageEvent(
                            (event.data as MessageTextEvent).chatUuid, event
                        )
                    }
                }
        }
    }

    private fun handleTextMessageEvent(chatId: String, event: KaonicEvent<KaonicEventData>) {

    }
}