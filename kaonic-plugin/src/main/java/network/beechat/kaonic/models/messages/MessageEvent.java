package network.beechat.kaonic.models.messages;

import androidx.annotation.NonNull;

import network.beechat.kaonic.models.KaonicEventData;

public abstract class MessageEvent extends KaonicEventData {
    public @NonNull String id;
    public @NonNull String chatUuid;

    public MessageEvent() {
        super();
        this.id = "";
        this.chatUuid = "";
    }

    public MessageEvent(@NonNull String address, long timestamp,
                        @NonNull String id,
                        @NonNull String chatUuid) {
        super(address, timestamp);
        this.id = id;
        this.chatUuid = chatUuid;
    }
}
