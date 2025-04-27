package network.beechat.kaonic.models.messages;

import androidx.annotation.NonNull;

public class MessageFileChunkEvent extends MessageEvent {
    public final byte[] bytes;

    public MessageFileChunkEvent(@NonNull String address, long timestamp,
                                 String id, String chatUuid, byte[] bytes) {
        super(address, timestamp, id, chatUuid);
        this.bytes = bytes;
    }
}
