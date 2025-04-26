package network.beechat.kaonic.sampleapp.nodedetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
    private val nodeAddress: String,
    private val chatService: ChatService
) : ViewModel() {
}