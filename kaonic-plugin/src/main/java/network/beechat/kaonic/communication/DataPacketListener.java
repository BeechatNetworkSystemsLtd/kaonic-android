package network.beechat.kaonic.communication;

import network.beechat.kaonic.models.Node;
import network.beechat.kaonic.models.datapacket.DataPacket;

public interface DataPacketListener {
    void onPacket(DataPacket packet, Node sender);
}
