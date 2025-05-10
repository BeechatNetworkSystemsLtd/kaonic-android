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
import network.beechat.kaonic.models.messages.MessageFileEvent
import network.beechat.kaonic.models.messages.MessageTextEvent
import network.beechat.kaonic.sampleapp.extensions.isMy

class ChatService(scope: CoroutineScope) {
    /**
     * key is address of chat id
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

                        is MessageFileEvent -> handleTextMessageEvent(
                            (event.data as MessageFileEvent).chatId,
                            event as KaonicEvent<MessageEvent>
                        )
                    }
                }
        }
    }

    fun createChatWithAddress(address: String): String {
        if (!contactChats.containsKey(address)) {
            val chatId = java.util.UUID.randomUUID().toString()
            contactChats[address] = chatId
            KaonicService.createChat(address, chatId)
        }

        return contactChats[address]!!
    }

    fun getChatMessages(chatId: String): StateFlow<ArrayList<KaonicEvent<MessageEvent>>> {
        return messages.getOrPut(chatId) { MutableStateFlow(arrayListOf()) }
    }

    private fun handleTextMessageEvent(chatId: String, event: KaonicEvent<MessageEvent>) {
        putChatIdIfNotExist(chatId, event.data.address)

        val flow = messages.getOrPut(chatId) { MutableStateFlow(arrayListOf()) }
        val oldList = flow.value
        val existingMessages = oldList.filter {
            it.data is MessageEvent && (it.data as MessageEvent).id == event.data.id
        }
        if (existingMessages.isNotEmpty()) {
            val index = oldList.indexOf(existingMessages.first())
            if (index != -1) {
                oldList.removeAt(index)
                oldList.add(index, event)
            }
            flow.value = oldList
        } else {
            val newList = ArrayList(oldList)
            newList.add(event)
            flow.value = newList
        }
    }

    fun sendTextMessage(message: String, address: String) {
        KaonicService.sendTextMessage(message, address, contactChats[address]!!)
    }

    fun sendFileMessage(filePath: String, address: String) {
        KaonicService.sendFileMessage(filePath, address, contactChats[address]!!)

    }

    private fun putChatIdIfNotExist(chatId: String, address: String) {
        if (!contactChats.containsKey(address)) {
            contactChats[address] = chatId
        }
    }
}