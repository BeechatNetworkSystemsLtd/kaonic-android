package network.beechat.kaonic.sampleapp.scan

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import network.beechat.kaonic.models.KaonicEventType
import network.beechat.kaonic.sampleapp.services.KaonicService

class ScanScreenViewModel : ViewModel() {
    private val _nodes = mutableStateListOf<String>()
    private val job: Job;
    val nodes: List<String> = _nodes

    init {
        job = CoroutineScope(Dispatchers.IO).launch {
            KaonicService.events
                .filter { event -> event.type == KaonicEventType.NODE_FOUND }
                .collect { event ->
                    addNode(event.address)
                }
        }

    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }

    fun addNode(newItem: String) {
        if (!_nodes.contains(newItem))
            _nodes.add(newItem)
    }
}