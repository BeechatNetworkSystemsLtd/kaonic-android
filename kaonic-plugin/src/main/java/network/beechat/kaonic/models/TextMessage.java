package network.beechat.kaonic.models;

import androidx.annotation.NonNull;

import network.beechat.kaonic.models.datapacket.messages.TextMessagePacket;

public class TextMessage extends Message {
    public final String message;

    public TextMessage(@NonNull String chatUuid, String message, @NonNull Node sender, @NonNull Node recipient) {
        super(chatUuid, sender, recipient);
        this.message = message;
    }

    public TextMessage(@NonNull String id, @NonNull String chatUuid, String message, @NonNull Node sender, @NonNull Node recipient) {
        super(id, chatUuid, sender, recipient);
        this.message = message;
    }

    public static TextMessage fromPacket(TextMessagePacket packet, Node sender, Node recipient) {
        return new TextMessage(packet.id, packet.chatUuid, packet.message, sender, recipient);
    }
}
