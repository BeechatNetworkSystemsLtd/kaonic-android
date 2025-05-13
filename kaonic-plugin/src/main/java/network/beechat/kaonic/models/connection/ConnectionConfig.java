package network.beechat.kaonic.models.connection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectionConfig {
     public ConnectionContact contact;
    public  List<Connection> connections;

    public ConnectionConfig(ConnectionContact contact, List<Connection> connections) {
        this.connections = connections;
        this.contact = contact;
    }
}
