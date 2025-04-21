package network.beechat.kaonic.models.datapacket.messages;

import com.fasterxml.jackson.core.JsonProcessingException;

import network.beechat.kaonic.communication.CborParser;
import network.beechat.kaonic.models.datapacket.DataPacketType;

public class FileChunkMessagePacket extends MessagePacket {
    public final byte[] bytes;

    public FileChunkMessagePacket(String id, String chatUuid, byte[] bytes) {
        super(id, chatUuid, DataPacketType.FILE_CHUNK_MESSAGE);
        this.bytes = bytes;
    }
}
