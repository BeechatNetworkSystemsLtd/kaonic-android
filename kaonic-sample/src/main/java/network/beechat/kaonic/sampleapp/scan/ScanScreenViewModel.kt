package network.beechat.kaonic.sampleapp.scan

import androidx.lifecycle.ViewModel
import network.beechat.kaonic.sampleapp.services.KaonicService

class ScanScreenViewModel : ViewModel() {
    val contacts: List<String> get() = KaonicService.contacts

    override fun onCleared() {
        super.onCleared()
    }
}