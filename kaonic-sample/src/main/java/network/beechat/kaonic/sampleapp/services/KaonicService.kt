package network.beechat.kaonic.sampleapp.services

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import network.beechat.kaonic.communication.KaonicCommunicationManager
import network.beechat.kaonic.communication.KaonicEventListener
import network.beechat.kaonic.models.KaonicEvent
import network.beechat.kaonic.models.KaonicEventData
import network.beechat.kaonic.models.KaonicEventType
import network.beechat.kaonic.models.connection.Connection
import network.beechat.kaonic.models.connection.ConnectionConfig
import network.beechat.kaonic.models.connection.ConnectionContact
import network.beechat.kaonic.models.connection.ConnectionInfo
import network.beechat.kaonic.models.connection.ConnectionType

object KaonicService : KaonicEventListener {
    private val TAG = "KaonicService"
    private lateinit var kaonicCommunicationHandler: KaonicCommunicationManager
    private lateinit var secureStorageHelper: SecureStorageHelper

    /// list of nodes
    private val _contacts = mutableStateListOf<String>()
    val contacts = _contacts

    /// stream of kaonic events
    private val _events = MutableSharedFlow<KaonicEvent<KaonicEventData>>()
    val events: SharedFlow<KaonicEvent<KaonicEventData>> = _events

    private var _myAddress = ""
    val myAddress: String
        get() = _myAddress

    fun init(
        kaonicCommunicationHandler: KaonicCommunicationManager,
        secureStorageHelper: SecureStorageHelper
    ) {
        this.kaonicCommunicationHandler = kaonicCommunicationHandler
        this.secureStorageHelper = secureStorageHelper
        kaonicCommunicationHandler.setEventListener(this)
        _myAddress = kaonicCommunicationHandler.myAddress

        kaonicCommunicationHandler.start(
            loadSecret(),
            ConnectionConfig(
                ConnectionContact("Kaonic"), arrayListOf(
                    Connection(
                        ConnectionType
                            .KaonicClient, ConnectionInfo("http://192.168.10.1:8080")
                    )
                )
            )
        )
        print("")
    }

    fun createChat(address: String, chatId: String) {
        kaonicCommunicationHandler.createChat(address, chatId)
    }

    fun sendTextMessage(message: String, address: String, chatId: String) {
        kaonicCommunicationHandler.sendMessage(address, message, chatId)
    }

    fun sendFileMessage(filePath: String, address: String, chatId: String) {
        kaonicCommunicationHandler.sendFile(filePath, address, chatId)
    }

    fun sendBroadcast(id: String, topic: String, bytes: ByteArray) {
        kaonicCommunicationHandler.sendBroadcast(id, topic, bytes)
    }

    fun sendConfig(
        mcs: Int,
        optionNumber: Int,
        module: Int,
        frequency: Int,
        channel: Int,
        channelSpacing: Int,
        txPower: Int
    ) {
        kaonicCommunicationHandler.sendConfig(
            mcs,
            optionNumber,
            module,
            frequency,
            channel,
            channelSpacing,
            txPower
        )
    }

    override fun onEventReceived(event: KaonicEvent<KaonicEventData>) {
        CoroutineScope(Dispatchers.Default).launch {
            // Log.i(TAG, "onEventReceived ${event.data.javaClass.name}")
            when (event.type) {
                KaonicEventType.MESSAGE_TEXT, KaonicEventType.MESSAGE_FILE,
                KaonicEventType.CHAT_CREATE -> {
                    _events.emit(event)
                }

                KaonicEventType.CONTACT_FOUND -> {
                    if (!contacts.contains(event.data.address))
                        contacts.add(event.data.address)
                }
            }
        }
    }

    private fun loadSecret(): String? {
        var secret: String? = null
        try {
            val SECRET_TAG = "KAONIC_SECRET"
            secret = secureStorageHelper.getSecured(SECRET_TAG)
            if (secret == null) {
                val messengerCreds = kaonicCommunicationHandler.generateSecret()
                secret = messengerCreds?.secret ?: ""
                secureStorageHelper.putSecured(SECRET_TAG, secret)
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "")
        }
        return secret
    }
}