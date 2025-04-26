package network.beechat.kaonic.sampleapp.services

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import network.beechat.kaonic.communication.KaonicEventListener
import network.beechat.kaonic.communication.LibCommunicationHandler
import network.beechat.kaonic.libsource.KaonicLib
import network.beechat.kaonic.models.KaonicEvent
import network.beechat.kaonic.models.KaonicEventData
import network.beechat.kaonic.models.KaonicEventType
import network.beechat.kaonic.models.messages.MessageTextEvent

object KaonicService : KaonicEventListener {
    private lateinit var kaonicCommunicationHandler: LibCommunicationHandler

    /// list of nodes
    private val nodes = mutableStateListOf<String>()

    /// stream of kaonic events
    private val _events = MutableSharedFlow<KaonicEvent<KaonicEventData>>()
    val events: SharedFlow<KaonicEvent<KaonicEventData>> = _events

    fun init(kaonicCommunicationHandler: LibCommunicationHandler) {
        this.kaonicCommunicationHandler = kaonicCommunicationHandler

    }

    fun sendTextMessage(message: String, address: String) {
        kaonicCommunicationHandler.sendMessage(address, message)
    }

    override fun onEventReceived(event: KaonicEvent<*>) {
        when (event.type) {
            KaonicEventType.MESSAGE_FILE_CHUNK -> {

            }

            KaonicEventType.MESSAGE_FILE_CHUNK -> {

            }
        }
    }
}