package network.beechat.kaonic.models;

import androidx.annotation.NonNull;

import network.beechat.kaonic.models.datapacket.messages.FileStartMessagePacket;


public class FileMessage extends Message {
    public final String fileName;
    public final int fileSize;
    public String localPath;

    public FileMessage(@NonNull String chatUuid, String fileName, int fileSize, @NonNull Node sender, @NonNull Node recipient) {
        super(chatUuid, sender, recipient);
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public FileMessage(@NonNull String id, @NonNull String chatUuid, String fileName, int fileSize, @NonNull Node sender, @NonNull Node recipient) {
        super(id, chatUuid, sender, recipient);
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public FileMessage(@NonNull String id, @NonNull String chatUuid, String fileName, int fileSize, String localPath, @NonNull Node sender, @NonNull Node recipient) {
        super(id, chatUuid, sender, recipient);
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.localPath = localPath;
    }

    public static FileMessage fromStartPacket(FileStartMessagePacket packet, Node sender, Node recipient) {
        return new FileMessage(packet.id, packet.chatUuid, packet.fileName, packet.fileSize, sender, recipient);
    }
}
