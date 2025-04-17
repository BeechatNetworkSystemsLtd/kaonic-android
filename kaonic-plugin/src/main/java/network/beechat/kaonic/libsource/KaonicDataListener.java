package network.beechat.kaonic.libsource;

import network.beechat.kaonic.models.Node;

interface KaonicDataListener {
    void onNodeFound(Node node);
    void onTextMessageReceived(Node node, String message);
    void onLocationMessageReceived(Node node, float latitude, float longitude);
    void onFileMessageReceived(Node node, byte[] bytes);
    void onCallStartedReceived();
}
