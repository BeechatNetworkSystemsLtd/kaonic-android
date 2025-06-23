package network.beechat.kaonic.models.messages;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

@Keep
public class MessageLocationEvent extends MessageEvent {
    public final float latitude;
    public final float longitude;

    public MessageLocationEvent(@NonNull String address, long timestamp,
                                String id, String chatUuid, float latitude,
                                float longitude) {
        super(address, timestamp, id, chatUuid);
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
