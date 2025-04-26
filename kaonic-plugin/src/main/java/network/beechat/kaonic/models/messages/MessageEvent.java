package network.beechat.kaonic.models.messages;

import network.beechat.kaonic.models.KaonicEventData;

public abstract class MessageEvent extends KaonicEventData {
    public final String id;
    public final String chatUuid;

    public MessageEvent(String id, String chatUuid) {
        this.id = id;
        this.chatUuid = chatUuid;
    }
}
