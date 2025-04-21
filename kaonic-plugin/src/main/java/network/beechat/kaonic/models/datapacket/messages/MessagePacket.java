package network.beechat.kaonic.models.datapacket.messages;

import network.beechat.kaonic.models.datapacket.DataPacket;

public abstract class MessagePacket extends DataPacket {
    public final String id;
    public final String chatUuid;

    public MessagePacket(String id, String chatUuid, int type) {
        super(type);
        this.id = id;
        this.chatUuid = chatUuid;
    }
}
