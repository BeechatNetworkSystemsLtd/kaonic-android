package network.beechat.kaonic.communication.base;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import network.beechat.kaonic.impl.KaonicLib;
import network.beechat.kaonic.models.MessengerCreds;
import network.beechat.kaonic.models.connection.ConnectionConfig;

public class KaonicBaseManager {
    final protected @NonNull KaonicLib kaonicLib;
    final protected ObjectMapper objectMapper = new ObjectMapper();

    public KaonicBaseManager(@NonNull KaonicLib kaonicLib) {
        this.kaonicLib = kaonicLib;
    }

    public boolean start(String secret, ConnectionConfig connectionConfig) {
        try {
            String json = objectMapper.writeValueAsString(connectionConfig);
            kaonicLib.start(
                    secret, json
            );
        } catch (JsonProcessingException e) {
            return false;
        }

        return true;
    }

    @Keep
    public void stop() {
        kaonicLib.stop();
    }


    public MessengerCreds generateSecret() {
        String jsonString = kaonicLib.generateSecret();
        try {
            return objectMapper.readValue(jsonString, MessengerCreds.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
