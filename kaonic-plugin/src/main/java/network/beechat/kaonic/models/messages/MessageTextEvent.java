package network.beechat.kaonic.models.messages;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class MessageTextEvent extends MessageEvent {
    public String message;

    public MessageTextEvent() {
        super();
    }

    public MessageTextEvent(@NonNull String address, long timestamp,
                            String id,
                            String chatUuid,
                            String message) {
        super(address, timestamp, id, chatUuid);
        this.message = message;
    }


    public MessageTextEvent(@NonNull String address, long timestamp,
                            String chatUuid,
                            String message) {
        this(address, timestamp, UUID.randomUUID().toString(), chatUuid, message);
    }
}
