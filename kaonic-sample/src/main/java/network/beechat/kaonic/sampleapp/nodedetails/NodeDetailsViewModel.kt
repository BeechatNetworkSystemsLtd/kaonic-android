package network.beechat.kaonic.sampleapp.nodedetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NodeDetailsViewModelFactory(private val address: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NodeDetailsViewModel(address) as T
    }
}

class NodeDetailsViewModel(private val nodeAddress:String):ViewModel() {
}