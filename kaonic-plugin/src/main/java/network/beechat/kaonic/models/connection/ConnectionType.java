package network.beechat.kaonic.models.connection;

import androidx.annotation.Keep;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
@Keep
public enum ConnectionType {
    TcpClient,
    KaonicClient
}
