package network.beechat.kaonic.models.datapacket.messages;

import network.beechat.kaonic.models.datapacket.DataPacketType;

public class TextMessagePacket extends MessagePacket {
    public final String message;

    public TextMessagePacket(String id, String chatUuid, String message) {
        super(id, chatUuid, DataPacketType.TEXT_MESSAGE);
        this.message = message;
    }
}
