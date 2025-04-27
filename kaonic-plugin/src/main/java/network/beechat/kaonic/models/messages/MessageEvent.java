package network.beechat.kaonic.models.messages;

import androidx.annotation.NonNull;

import network.beechat.kaonic.models.KaonicEventData;

public abstract class MessageEvent extends KaonicEventData {
    public @NonNull String id;
    public @NonNull String chatId;

    public MessageEvent() {
        super();
        this.id = "";
        this.chatId = "";

    }

    public MessageEvent(@NonNull String address, long timestamp,
                        @NonNull String id,
                        @NonNull String chatId) {
        super(address, timestamp);
        this.id = id;
        this.chatId = chatId;
    }
}
