package network.beechat.kaonic.communication;

import network.beechat.kaonic.models.DataPacket;

public interface DataPacketListener {
    void onPacket(DataPacket packet);
}
