package network.beechat.kaonic.models.messages;

import androidx.annotation.NonNull;

import java.util.UUID;

public class MessageTextEvent extends MessageEvent {
    public String text;

    public MessageTextEvent() {
        super();
    }

    public MessageTextEvent(@NonNull String address, long timestamp,
                            String id,
                            String chatUuid,
                            String text) {
        super(address, timestamp, id, chatUuid);
        this.text = text;
    }


    public MessageTextEvent(@NonNull String address, long timestamp,
                            String chatUuid,
                            String text) {
        this(address, timestamp, UUID.randomUUID().toString(), chatUuid, text);
    }
}
