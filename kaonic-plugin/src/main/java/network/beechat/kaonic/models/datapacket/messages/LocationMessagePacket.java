package network.beechat.kaonic.models.datapacket.messages;

import network.beechat.kaonic.models.datapacket.DataPacketType;

public class LocationMessagePacket extends MessagePacket {
    public final float latitude;
    public final float longitude;

    public LocationMessagePacket(String id, String chatUuid,float latitude, float longitude) {
        super(id,chatUuid, DataPacketType.LOCATION_MESSAGE);
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
