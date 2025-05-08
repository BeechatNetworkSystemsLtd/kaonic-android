package network.beechat.kaonic.models.messages;

import androidx.annotation.NonNull;


public class MessageFileEvent extends MessageEvent {
    public final @NonNull String fileName;
    public final int fileSize;
    public int fileSizeProcessed = 0;
    public String path;

    public MessageFileEvent(@NonNull String address, long timestamp,
                            String id, String chatUuid, @NonNull String fileName,
                            int fileSize) {
        super(address, timestamp, id, chatUuid);
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
}
