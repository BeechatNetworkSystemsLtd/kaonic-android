package network.beechat.kaonic.models.messages;

public class MessageFileStartEvent extends MessageEvent {
    public final String fileName;
    public final int fileSize;

    public MessageFileStartEvent(String id, String chatUuid, String fileName, int fileSize) {
        super(id, chatUuid);
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
}
