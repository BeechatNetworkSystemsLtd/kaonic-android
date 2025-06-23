package network.beechat.kaonic.models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

@Keep
public class BroadcastEventData extends KaonicEventData {
    public @NonNull String id;
    public @NonNull String topic;
    public @NonNull byte[] bytes;

    @Keep
    public BroadcastEventData() {
        super();
        id = "";
        topic = "";
        bytes = new byte[]{};
    }

    public BroadcastEventData(@NonNull String address, @NonNull String id,
                       @NonNull String topic, @NonNull byte[] bytes) {
        super(address, System.currentTimeMillis());
        this.id = id;
        this.topic = topic;
        this.bytes = bytes;
    }
}
