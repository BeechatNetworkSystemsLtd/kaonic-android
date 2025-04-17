package network.beechat.kaonic.communication;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import network.beechat.kaonic.libsource.Kaonic;
import network.beechat.kaonic.models.FileMessage;
import network.beechat.kaonic.models.LocationMessage;
import network.beechat.kaonic.models.Message;
import network.beechat.kaonic.models.Node;
import network.beechat.kaonic.models.TextMessage;

public class CommunicationService {
    private final Node me;
    private final Kaonic kaonic;
    // chatCallback for all chats with all users
    private ChatCallback chatCallback;
    private final ArrayList<Message> messages = new ArrayList<>();

    public CommunicationService(Node me, Kaonic kaonic) {
        this.me = me;
        this.kaonic = kaonic;
    }

    public List<Message> getChatMessages(Node node) {
        return messages.stream()
                .filter(m -> m.sender == me && m.recipient == node || m.sender == node && m.recipient == me)
                .collect(Collectors.toList());
    }

    public void initializeService(ChatCallback chatCallback) {
        this.chatCallback = chatCallback;
        //smth with kaonic
    }

    public void sendTextMessage(String message, Node recipient) {
        Message textMessage = new TextMessage(message, me, recipient);
        sendMessage(textMessage);
    }

    public void sentFileMessage(String pathToFile, Node recipient) {
        String filename = pathToFile.substring(pathToFile.lastIndexOf("/") + 1);
        Message fileMessage = new FileMessage(filename, me, recipient);
        sendMessage(fileMessage);
    }

    public void sentLocationMessage(float latitude, float longitude, Node recipient) {
        Message locationMessage = new LocationMessage(latitude, longitude, me, recipient);
        sendMessage(locationMessage);
    }

    private void sendMessage(Message message) {
        //kaonic send command

        if (chatCallback != null) {
            messages.add(message);
            chatCallback.onMessageSent(message);
        }
    }

}
