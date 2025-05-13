package network.beechat.kaonic.models.connection;


public class Connection {
    public ConnectionType type;
    public ConnectionInfo info;

    public Connection(ConnectionType type, ConnectionInfo info) {
        this.info = info;
        this.type = type;
    }
}
