package network.beechat.kaonic.sampleapp.scan

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class ScanScreenViewModel : ViewModel(){
    private val _nodes = mutableStateListOf<String>()
    val nodes: List<String> = _nodes

    fun addNode(newItem: String) {
        _nodes.add(newItem)
    }
}