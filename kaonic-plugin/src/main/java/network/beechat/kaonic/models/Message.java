package network.beechat.kaonic.models;

import androidx.annotation.NonNull;

import java.util.UUID;

public abstract class Message {
    public final long timestamp;
    public final @NonNull String uuid;
    public final @NonNull String chatUuid;
    public final @NonNull Node sender;
    public final @NonNull Node recipient;

    public Message(@NonNull String chatUuid, long timestamp, @NonNull Node sender, @NonNull Node recipient) {
        this.timestamp = timestamp;
        uuid = UUID.randomUUID().toString();
        this.chatUuid = chatUuid;
        this.recipient = recipient;
        this.sender = sender;
    }

    public Message(@NonNull String chatUuid, @NonNull Node sender, @NonNull Node recipient) {
        this.timestamp = System.currentTimeMillis();
        uuid = UUID.randomUUID().toString();
        this.chatUuid = chatUuid;
        this.recipient = recipient;
        this.sender = sender;
    }

    public Message(@NonNull String id, @NonNull String chatUuid, @NonNull Node sender, @NonNull Node recipient) {
        this.timestamp = System.currentTimeMillis();
        uuid = id;
        this.chatUuid = chatUuid;
        this.recipient = recipient;
        this.sender = sender;
    }
}