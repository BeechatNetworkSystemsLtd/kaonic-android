package network.beechat.kaonic.models;

import androidx.annotation.NonNull;

public class LocationMessage extends Message {
    public final float latitude;
    public final float longitude;

    public LocationMessage(float latitude, float longitude, @NonNull Node sender, @NonNull Node recipient) {
        super(sender, recipient);
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
