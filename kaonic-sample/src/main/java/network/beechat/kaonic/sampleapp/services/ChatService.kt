package network.beechat.kaonic.sampleapp.services

import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import network.beechat.kaonic.models.KaonicEvent
import network.beechat.kaonic.models.KaonicEventType
import network.beechat.kaonic.models.messages.MessageEvent
import network.beechat.kaonic.models.messages.MessageTextEvent

class ChatService(scope: CoroutineScope) {
    /**
     * key is address of the chat,
     * meaning it's chatUUID
     */
    private val messages =
        mutableStateMapOf<String, MutableStateFlow<ArrayList<KaonicEvent<MessageEvent>>>>()

    /**
     * key is contact address,
     * value is chatUUID
     */
    private val contactChats = mutableStateMapOf<String, String>()

    init {
        scope.launch {
            KaonicService.events
                .filter { event -> KaonicEventType.messageEvents.contains(event.type) }
                .collect { event ->
                    when (event.data) {
                        is MessageTextEvent -> handleTextMessageEvent(
                            (event.data as MessageTextEvent).chatId,
                            event as KaonicEvent<MessageEvent>
                        )
                    }
                }
        }
    }

    fun getChatMessages(address: String): StateFlow<ArrayList<KaonicEvent<MessageEvent>>> {
        val chatId: String
        if (!contactChats.containsKey(address)) {
            chatId = java.util.UUID.randomUUID().toString()
            contactChats[chatId] = address
        } else {
            chatId = contactChats[address] ?: java.util.UUID.randomUUID().toString()
        }
        return messages.getOrPut(chatId) { MutableStateFlow(arrayListOf()) }
    }

    private fun handleTextMessageEvent(chatId: String, event: KaonicEvent<MessageEvent>) {
        checkChatId(chatId, event.data.address)

        val flow = messages.getOrPut(chatId) { MutableStateFlow(arrayListOf()) }
        val oldList = flow.value
        val newList = ArrayList(oldList)
        newList.add(event)
        flow.value = newList
    }

    fun sendTextMessage(message: String, address: String) {
        KaonicService.sendTextMessage(message, address, contactChats[address] ?: "")
    }

    private fun checkChatId(chatId: String, address: String) {
        if (!contactChats.containsKey(address)) {
            contactChats[address] = chatId
        }
    }
}