package network.beechat.kaonic.sampleapp.services

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import network.beechat.kaonic.communication.KaonicEventListener
import network.beechat.kaonic.communication.LibCommunicationHandler
import network.beechat.kaonic.models.KaonicEvent
import network.beechat.kaonic.models.KaonicEventData
import network.beechat.kaonic.models.KaonicEventType

object KaonicService : KaonicEventListener {
    private val TAG = "KaonicService"
    private lateinit var kaonicCommunicationHandler: LibCommunicationHandler

    /// list of nodes
    private val _contacts = mutableStateListOf<String>()
    val contacts = _contacts

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

    suspend fun emitNodeFound() {
        flow {
            repeat(4) { index ->
                emit("Item $index")
                delay(500)
            }
        }.collect { generatedString ->
            contacts.add(generatedString)
        }
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

                KaonicEventType.CONTACT_FOUND -> {
                    if (!contacts.contains(event.data.address))
                        contacts.add(event.data.address)
                }
            }
        }
    }
}