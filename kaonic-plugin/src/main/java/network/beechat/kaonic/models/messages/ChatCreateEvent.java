package network.beechat.kaonic.models.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import network.beechat.kaonic.models.KaonicEventData;

public class ChatCreateEvent extends KaonicEventData {
    @JsonProperty("chat_id")
    final String chatId;
    @JsonProperty("chat_name")
    final String chatName;

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
