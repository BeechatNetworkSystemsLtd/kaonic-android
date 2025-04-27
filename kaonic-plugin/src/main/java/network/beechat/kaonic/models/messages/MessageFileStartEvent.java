package network.beechat.kaonic.models.messages;

import androidx.annotation.NonNull;

public class MessageFileStartEvent extends MessageEvent {
    public final String fileName;
    public final int fileSize;

    public MessageFileStartEvent(@NonNull String address, long timestamp,
                                 String id, String chatUuid, String fileName,
                                 int fileSize) {
        super(address, timestamp,id, chatUuid);
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
}
