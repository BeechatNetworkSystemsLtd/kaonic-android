package network.beechat.kaonic.models.messages;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * THIS IS INTERNAL LIB EVENT
 * YOU WONT RECEIVE IT
 */
public class MessageFileStartEvent extends MessageEvent {
    @JsonProperty("file_id")
    public final String fileId;
    @JsonProperty("file_name")
    public final String fileName;
    @JsonProperty("file_size")
    public final int fileSize;

    public MessageFileStartEvent() {
        super("", 0, "", "");
        this.fileId = id;
        this.fileName = "";
        this.fileSize = 0;
    }

    public MessageFileStartEvent(@NonNull String address, long timestamp,
                                 String id, String chatUuid, String fileName,
                                 int fileSize) {
        super(address, timestamp, id, chatUuid);
        this.fileId = id;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
}
