package network.beechat.kaonic.communication;

import android.util.Log;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import network.beechat.kaonic.libsource.KaonicDataChannelListener;
import network.beechat.kaonic.libsource.KaonicLib;
import network.beechat.kaonic.models.KaonicEvent;
import network.beechat.kaonic.models.KaonicEventData;
import network.beechat.kaonic.models.KaonicEventType;
import network.beechat.kaonic.models.messages.MessageFileChunkEvent;
import network.beechat.kaonic.models.messages.MessageFileStartEvent;
import network.beechat.kaonic.models.messages.MessageLocationEvent;
import network.beechat.kaonic.models.messages.MessageTextEvent;

public class LibCommunicationHandler implements KaonicDataChannelListener {
    final private String TAG = "LibCommunicationHandler";

    final private @NonNull KaonicLib kaonicLib;
    final private ObjectMapper objectMapper = new ObjectMapper();
    private KaonicEventListener eventListener;

    public LibCommunicationHandler(@NonNull KaonicLib kaonicLib) {
        this.kaonicLib = kaonicLib;
        kaonicLib.setChannelListener(this);
    }

    public void onDestroy() {
        kaonicLib.removeChannelListener();
    }

    public void setEventListener(KaonicEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void removePacketListener() {
        this.eventListener = null;
    }

    public void transmitData(KaonicEvent kaonicEvent) {
        try {
            String jsonString = objectMapper.writeValueAsString(kaonicEvent);
            kaonicLib.transmit(jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDataReceive(String dataJson) {
        try {
            JSONObject eventObject = new JSONObject(dataJson);
            String eventType = eventObject.getString("type");
            String address = eventObject.getString("address");
            long timestamp = eventObject.getLong("timestamp");
            JSONObject eventData = eventObject.getJSONObject("data");
            KaonicEventData kaonicEventData = null;
            switch (eventType) {
                case KaonicEventType.MESSAGE_TEXT:
                    kaonicEventData = objectMapper.readValue(eventData.toString(),
                            MessageTextEvent.class);
                case KaonicEventType.MESSAGE_LOCATION:
                    kaonicEventData = objectMapper.readValue(eventData.toString(),
                            MessageLocationEvent.class);
                case KaonicEventType.MESSAGE_FILE_START:
                    kaonicEventData = objectMapper.readValue(eventData.toString(),
                            MessageFileStartEvent.class);
                case KaonicEventType.MESSAGE_FILE_CHUNK:
                    kaonicEventData = objectMapper.readValue(eventData.toString(),
                            MessageFileChunkEvent.class);
            }
            if (kaonicEventData != null) {
                KaonicEvent event = new KaonicEvent(eventType, address, timestamp);
                event.data = kaonicEventData;
                eventListener.onEventReceived(event);
            }
        } catch (JSONException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
