package network.beechat.kaonic.models.connection;

import androidx.annotation.Keep;

@Keep
public class ConnectionInfo {
    public  String address;

    public ConnectionInfo(String address) {
        this.address = address;
    }
}
