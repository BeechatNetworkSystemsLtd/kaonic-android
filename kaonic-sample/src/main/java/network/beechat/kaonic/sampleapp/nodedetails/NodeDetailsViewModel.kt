package network.beechat.kaonic.sampleapp.nodedetails

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.StateFlow
import network.beechat.kaonic.models.KaonicEvent
import network.beechat.kaonic.models.messages.MessageEvent
import network.beechat.kaonic.sampleapp.services.ChatService

class NodeDetailsViewModelFactory(
    private val address: String,
    private val chatService: ChatService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NodeDetailsViewModel(address, chatService) as T
    }
}

class NodeDetailsViewModel(
    val nodeAddress: String,
    private val chatService: ChatService
) : ViewModel() {
    private val _messages = mutableStateOf<List<KaonicEvent<MessageEvent>>>(emptyList())
    val messages: List<KaonicEvent<MessageEvent>> get() = _messages.value
    val chatId: String = chatService.createChatWithAddress(nodeAddress)

    fun getMessages(address: String): StateFlow<List<KaonicEvent<MessageEvent>>> {
        return chatService.getChatMessages(chatId)
    }

    fun sendMessage(message: String) {
        chatService.sendTextMessage(message, nodeAddress)
    }

    fun sendFile(fileUri: Uri) {
        chatService.sendFileMessage(fileUri.toString(), nodeAddress)
    }

}