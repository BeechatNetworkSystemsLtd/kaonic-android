package network.beechat.kaonic.models.messages;

import androidx.annotation.Keep;

import com.fasterxml.jackson.annotation.JsonProperty;

import network.beechat.kaonic.models.KaonicEventData;

@Keep
public class ChatCreateEvent extends KaonicEventData {
    @JsonProperty("chat_id")
    public final String chatId;
    @JsonProperty("chat_name")
    public final String chatName;

    @Keep
    public ChatCreateEvent() {
        super("", System.currentTimeMillis());
        this.chatId = "";
        this.chatName = "";

    }

    public ChatCreateEvent(String address, String chatId) {
        super(address, System.currentTimeMillis());
        this.chatId = chatId;
        this.chatName = "";
    }
}
