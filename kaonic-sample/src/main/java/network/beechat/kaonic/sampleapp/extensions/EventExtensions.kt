package network.beechat.kaonic.sampleapp.extensions

import network.beechat.kaonic.models.KaonicEvent
import network.beechat.kaonic.models.messages.MessageEvent
import network.beechat.kaonic.sampleapp.services.KaonicService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun KaonicEvent<MessageEvent>.isMy(): Boolean {
    return KaonicService.myAddress == data.address
}

fun KaonicEvent<MessageEvent>.timeFormatted(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date = Date(data.timestamp)
    return sdf.format(date)
}
