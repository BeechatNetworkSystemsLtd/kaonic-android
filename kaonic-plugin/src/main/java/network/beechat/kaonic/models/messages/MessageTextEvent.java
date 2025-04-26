package network.beechat.kaonic.models.messages;

public class MessageTextEvent extends MessageEvent {
    public final String message;

    public MessageTextEvent(String id, String chatUuid, String message) {
        super(id, chatUuid);
        this.message = message;
    }
}
