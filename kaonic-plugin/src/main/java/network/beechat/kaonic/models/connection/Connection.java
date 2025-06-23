package network.beechat.kaonic.models.connection;


import androidx.annotation.Keep;

@Keep
public class Connection {
    public ConnectionType type;
    public ConnectionInfo info;

    public Connection(ConnectionType type, ConnectionInfo info) {
        this.info = info;
        this.type = type;
    }
}
