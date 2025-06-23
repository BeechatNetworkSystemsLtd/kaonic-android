package network.beechat.kaonic.models.messages;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import java.util.UUID;

@Keep
public class MessageTextEvent extends MessageEvent {
    public String text;

    @Keep
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
