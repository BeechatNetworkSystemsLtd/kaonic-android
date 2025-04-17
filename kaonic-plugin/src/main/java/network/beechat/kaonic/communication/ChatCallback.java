package network.beechat.kaonic.communication;


import network.beechat.kaonic.models.Message;

public interface ChatCallback {
    void onMessageReceived(Message message);

    void onMessageSent(Message message);

    void onMessageUpdated(Message message);
}
