package network.beechat.kaonic.sampleapp.services

import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import network.beechat.kaonic.models.KaonicEventType
import network.beechat.kaonic.models.messages.MessageEvent
import network.beechat.kaonic.models.messages.MessageTextEvent

class ChatService(scope: CoroutineScope) {
    private val messages = mutableStateMapOf<String, ArrayList<MessageEvent>>()

    init {
        scope.launch {
            KaonicService.events
                .filter { event -> KaonicEventType.messageEvents.contains(event.type) }
                .collect { event ->
                    when (event.data) {
                        is MessageTextEvent -> handleTextMessageEvent(
                            event.address,
                            event.data as MessageEvent
                        )
                    }
                }
        }
    }

    fun sendTextMessage(message: String, address: String) {
        KaonicService.sendTextMessage(message, address)
    }

    private fun handleTextMessageEvent(address: String, event: MessageEvent) {
        if (!messages.containsKey(address)) {
            messages[address] = arrayListOf()
        }
        messages[address]!!.add(event)
    }
}