package network.beechat.kaonic.models.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class MessageTextEvent extends MessageEvent {
    public String message;

    public MessageTextEvent() {
        super();
    }

    public MessageTextEvent(String id,
                            String chatUuid,
                            String message) {
        super(id, chatUuid);
        this.message = message;
    }


    public MessageTextEvent(String chatUuid, String message) {
        this(UUID.randomUUID().toString(), chatUuid, message);
    }
}
