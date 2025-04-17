package network.beechat.kaonic.models;

import androidx.annotation.NonNull;

import java.util.UUID;

public abstract class Message {
    public final long timestamp;
    public final @NonNull String uuid;
    public final @NonNull Node sender;
    public final @NonNull Node recipient;

    public Message(long timestamp, @NonNull Node sender, @NonNull Node recipient) {
        this.timestamp = timestamp;
        uuid = UUID.randomUUID().toString();
        this.recipient = recipient;
        this.sender = sender;
    }

    public Message(@NonNull Node sender, @NonNull Node recipient) {
        this.timestamp = System.currentTimeMillis();
        uuid = UUID.randomUUID().toString();
        this.recipient = recipient;
        this.sender = sender;
    }
}