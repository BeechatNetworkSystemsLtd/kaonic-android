package network.beechat.kaonic.models.datapacket.messages;

import network.beechat.kaonic.models.datapacket.DataPacketType;

public class FileStartMessagePacket extends MessagePacket {
    public final String fileName;
    public final int fileSize;

    public FileStartMessagePacket(String id, String chatUuid, String fileName, int fileSize) {
        super(id, chatUuid, DataPacketType.FILE_START_MESSAGE);
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
}
