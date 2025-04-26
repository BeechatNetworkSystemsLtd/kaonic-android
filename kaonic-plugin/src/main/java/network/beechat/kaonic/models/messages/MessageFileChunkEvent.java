package network.beechat.kaonic.models.messages;

public class MessageFileChunkEvent extends MessageEvent {
    public final byte[] bytes;

    public MessageFileChunkEvent(String id, String chatUuid, byte[] bytes) {
        super(id, chatUuid);
        this.bytes = bytes;
    }
}
