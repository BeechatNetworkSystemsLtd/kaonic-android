package network.beechat.kaonic.models.datapacket;

import com.fasterxml.jackson.core.JsonProcessingException;

import network.beechat.kaonic.communication.CborParser;

/**
 * Data Packets are classes to transmit to
 * Kaonic lib directly in CBOR format
 */
public class DataPacket {
    public final int type;

    public DataPacket(int type) {
        this.type = type;
    }
}
