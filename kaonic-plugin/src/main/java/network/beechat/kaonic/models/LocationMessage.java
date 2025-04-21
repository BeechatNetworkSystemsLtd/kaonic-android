package network.beechat.kaonic.models;

import androidx.annotation.NonNull;

import network.beechat.kaonic.models.datapacket.messages.LocationMessagePacket;
import network.beechat.kaonic.models.datapacket.messages.TextMessagePacket;

public class LocationMessage extends Message {
    public final float latitude;
    public final float longitude;

    public LocationMessage(@NonNull String chatUuid, float latitude, float longitude, @NonNull Node sender, @NonNull Node recipient) {
        super(chatUuid, sender, recipient);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationMessage(@NonNull String id, @NonNull String chatUuid, float latitude, float longitude, @NonNull Node sender, @NonNull Node recipient) {
        super(id, chatUuid, sender, recipient);
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public static LocationMessage fromPacket(LocationMessagePacket packet, Node sender, Node recipient) {
        return new LocationMessage(packet.id, packet.chatUuid, packet.latitude, packet.longitude, sender, recipient);
    }
}
