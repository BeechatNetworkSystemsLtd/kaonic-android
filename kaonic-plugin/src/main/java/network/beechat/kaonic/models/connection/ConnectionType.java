package network.beechat.kaonic.models.connection;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ConnectionType {
    TcpClient,
    KaonicClient
}
