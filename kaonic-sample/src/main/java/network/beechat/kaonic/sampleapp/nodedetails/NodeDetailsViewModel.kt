package network.beechat.kaonic.sampleapp.nodedetails

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.StateFlow
import network.beechat.kaonic.models.KaonicEvent
import network.beechat.kaonic.models.messages.MessageEvent
import network.beechat.kaonic.sampleapp.services.ChatService
import network.beechat.kaonic.sampleapp.services.KaonicService
import network.beechat.kaonic.sampleapp.services.VideoStreamingService

class NodeDetailsViewModelFactory(
    private val address: String,
    private val chatService: ChatService,
    private val videoStreamingService: VideoStreamingService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NodeDetailsViewModel(address, chatService, videoStreamingService) as T
    }
}

class NodeDetailsViewModel(
    val nodeAddress: String,
    private val chatService: ChatService,
    private val videoStreamingService: VideoStreamingService
) : ViewModel() {
    private val _messages = mutableStateOf<List<KaonicEvent<MessageEvent>>>(emptyList())
    val messages: List<KaonicEvent<MessageEvent>> get() = _messages.value
    var chatId: String = chatService.createChatWithAddress(nodeAddress)
    private val _videoStreamingActive = mutableStateOf(false)
    val videoStreamingActive: Boolean get() = _videoStreamingActive.value

    fun getMessages(address: String): StateFlow<List<KaonicEvent<MessageEvent>>> {
        chatId = chatService.createChatWithAddress(nodeAddress)
        return chatService.getChatMessages(chatId)
    }

    fun sendMessage(message: String) {
        chatService.sendTextMessage(message, nodeAddress)
    }

    fun sendFile(fileUri: Uri) {
        chatService.sendFileMessage(fileUri.toString(), nodeAddress)
    }

    fun startVideoStream() {
        videoStreamingService.stopListenVideoStream()
        videoStreamingService.startListenVideoStreamForAddress(nodeAddress, chatId)
        _videoStreamingActive.value = true
    }

    fun stopVideoStream() {
        videoStreamingService.stopListenVideoStream()
        _videoStreamingActive.value = false
    }

}