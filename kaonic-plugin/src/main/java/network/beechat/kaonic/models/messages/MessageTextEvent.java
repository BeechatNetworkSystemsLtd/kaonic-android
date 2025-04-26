package network.beechat.kaonic.models.messages;

import java.util.UUID;

public class MessageTextEvent extends MessageEvent {
    public final String message;

    public MessageTextEvent(String id, String chatUuid, String message) {
        super(id, chatUuid);
        this.message = message;
    }


    public MessageTextEvent(String chatUuid, String message) {
        super(UUID.randomUUID().toString(), chatUuid);
        this.message = message;
    }
}
