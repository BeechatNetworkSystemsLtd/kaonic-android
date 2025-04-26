package network.beechat.kaonic.models.messages;

public class MessageLocationEvent extends MessageEvent {
    public final float latitude;
    public final float longitude;

    public MessageLocationEvent(String id, String chatUuid, float latitude, float longitude) {
        super(id, chatUuid);
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
