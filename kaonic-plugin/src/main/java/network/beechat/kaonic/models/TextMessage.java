package network.beechat.kaonic.models;

import androidx.annotation.NonNull;

public class TextMessage extends Message {
    public final String message;

    public TextMessage(String message, @NonNull Node sender, @NonNull Node recipient) {
        super(sender, recipient);
        this.message = message;
    }
}
