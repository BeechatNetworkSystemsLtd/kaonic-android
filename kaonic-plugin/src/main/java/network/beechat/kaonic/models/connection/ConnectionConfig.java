package network.beechat.kaonic.models.connection;

import androidx.annotation.Keep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@Keep
public class ConnectionConfig {
     public ConnectionContact contact;
    public  List<Connection> connections;

    @Keep
    public ConnectionConfig(ConnectionContact contact, List<Connection> connections) {
        this.connections = connections;
        this.contact = contact;
    }
}
