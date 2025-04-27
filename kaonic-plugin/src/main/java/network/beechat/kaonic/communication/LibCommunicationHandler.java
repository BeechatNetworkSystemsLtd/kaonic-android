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
import network.beechat.kaonic.models.ContactFoundEvent;
import network.beechat.kaonic.models.calls.CallAnswerEvent;
import network.beechat.kaonic.models.calls.CallNewEvent;
import network.beechat.kaonic.models.calls.CallRejectEvent;
import network.beechat.kaonic.models.calls.CallVoiceEvent;
import network.beechat.kaonic.models.messages.MessageFileChunkEvent;
import network.beechat.kaonic.models.messages.MessageFileStartEvent;
import network.beechat.kaonic.models.messages.MessageLocationEvent;
import network.beechat.kaonic.models.messages.MessageTextEvent;

public class LibCommunicationHandler implements KaonicDataChannelListener {
    final private String TAG = "LibCommunicationHandler";

    final private @NonNull KaonicLib kaonicLib;
    final private ObjectMapper objectMapper = new ObjectMapper();
    private KaonicEventListener eventListener;
    private String myAddress = "1234567890";

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

    public String getMyAddress() {
        return myAddress;
    }

    public void sendMessage(String address, String message) {
        transmitData(new KaonicEvent(KaonicEventType.MESSAGE_TEXT,
                new MessageTextEvent(address, System.currentTimeMillis(), address, message)));
        try {
            onDataReceive(objectMapper.writeValueAsString(new KaonicEvent(KaonicEventType.MESSAGE_TEXT,
                    new MessageTextEvent(myAddress, System.currentTimeMillis(), address, message))));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void transmitData(KaonicEvent kaonicEvent) {
        try {
            String jsonString = objectMapper.writeValueAsString(kaonicEvent);
            Log.i(TAG, "\uD83D\uDD3C Kaonic data sent:" + jsonString);
            kaonicLib.transmit(jsonString);
        } catch (JsonProcessingException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDataReceive(String dataJson) {
        Log.i(TAG, "\uD83D\uDD3D Kaonic data received:" + dataJson);
        try {
            JSONObject eventObject = new JSONObject(dataJson);
            String eventType = eventObject.getString("type");
            JSONObject eventData = eventObject.getJSONObject("data");
            String address = eventData.getString("address");
            long timestamp = System.currentTimeMillis();//eventData.get("timestamp");
            KaonicEventData kaonicEventData = null;
            switch (eventType) {
                case KaonicEventType.CONTACT_FOUND:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), ContactFoundEvent.class);
                    break;
                case KaonicEventType.MESSAGE_TEXT:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), MessageTextEvent.class);
                    break;
                case KaonicEventType.MESSAGE_LOCATION:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), MessageLocationEvent.class);
                    break;
                case KaonicEventType.MESSAGE_FILE_START:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), MessageFileStartEvent.class);
                    break;
                case KaonicEventType.MESSAGE_FILE_CHUNK:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), MessageFileChunkEvent.class);
                    break;
                case KaonicEventType.CALL_NEW:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), CallNewEvent.class);
                    break;
                case KaonicEventType.CALL_ANSWER:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), CallAnswerEvent.class);
                    break;
                case KaonicEventType.CALL_VOICE:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), CallVoiceEvent.class);
                    break;
                case KaonicEventType.CALL_REJECT:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), CallRejectEvent.class);
            }
            if (kaonicEventData != null) {
                KaonicEvent event = new KaonicEvent(eventType);
                event.data = kaonicEventData;

                Log.i(TAG, "\uD83D\uDCDA onEventReceived called");
                if (eventListener != null) {
                    eventListener.onEventReceived(event);
                }
            }
        } catch (JSONException | JsonProcessingException e ) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }
}
