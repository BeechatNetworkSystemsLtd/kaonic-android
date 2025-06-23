package network.beechat.kaonic.models;

import androidx.annotation.Keep;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Keep
public class MessengerCreds {
    @JsonProperty("my_address")
    public String myAddress;
    @JsonProperty
    public String secret;

    @Keep
    public MessengerCreds() {
        this.myAddress = "";
        this.secret = "";
    }

    @Keep
    public MessengerCreds(String myAddress, String secret) {
        this.myAddress = myAddress;
        this.secret = secret;
    }
}
