package network.beechat.kaonic.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessengerCreds {
    @JsonProperty("my_address")
    public String myAddress;
    public String secret;

    public MessengerCreds() {
        this.myAddress = "";
        this.secret = "";
    }

    public MessengerCreds(String myAddress, String secret) {
        this.myAddress = myAddress;
        this.secret = secret;
    }
}
