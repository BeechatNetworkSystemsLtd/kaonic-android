package network.beechat.kaonic.sampleapp.services

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import network.beechat.kaonic.communication.KaonicEventListener
import network.beechat.kaonic.communication.LibCommunicationHandler
import network.beechat.kaonic.libsource.KaonicLib
import network.beechat.kaonic.models.KaonicEvent
import network.beechat.kaonic.models.KaonicEventData
import network.beechat.kaonic.models.KaonicEventType
import network.beechat.kaonic.models.messages.MessageTextEvent

object KaonicService : KaonicEventListener {
    private val TAG = "KaonicService"
    private lateinit var kaonicCommunicationHandler: LibCommunicationHandler

    /// list of nodes
    private val nodes = mutableStateListOf<String>()

    /// stream of kaonic events
    private val _events = MutableSharedFlow<KaonicEvent<KaonicEventData>>()
    val events: SharedFlow<KaonicEvent<KaonicEventData>> = _events

    private var _myAddress = ""
    val myAddress = _myAddress;

    fun init(kaonicCommunicationHandler: LibCommunicationHandler) {
        this.kaonicCommunicationHandler = kaonicCommunicationHandler
        kaonicCommunicationHandler.setEventListener(this)
        _myAddress = kaonicCommunicationHandler.myAddress;
    }

    fun sendTextMessage(message: String, address: String) {
        kaonicCommunicationHandler.sendMessage(address, message)
    }

    override fun onEventReceived(event: KaonicEvent<KaonicEventData>) {
        CoroutineScope(Dispatchers.Default).launch {
            Log.i(TAG, "onEventReceived ${event.data.javaClass.name}")
            when (event.type) {
                KaonicEventType.MESSAGE_TEXT -> {
                    _events.emit(event)
                }
            }
        }
    }
}