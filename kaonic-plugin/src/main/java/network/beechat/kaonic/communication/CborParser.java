package network.beechat.kaonic.communication;

import android.content.Context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

import java.io.IOException;


public class CborParser {
    private static CborParser instance;
    private final CBORMapper mapper;

    private CborParser() {
        mapper = new CBORMapper();
    }

    public static synchronized CborParser getInstance() {
        if (instance == null) {
            instance = new CborParser();
        }
        return instance;
    }

    public <T> byte[] toBytes(T data) throws JsonProcessingException {
        return mapper.writeValueAsBytes(data);
    }

    public <T> T toObj(byte[] bytes, Class<T> classType) throws IOException {
        return mapper.readValue(bytes, classType);
    }
}
