package network.beechat.kaonic.models.connection;

import androidx.annotation.Keep;

@Keep
public class ConnectionContact {
    public  String name;

    @Keep
    public ConnectionContact(String name) {
        this.name = name;
    }
}
