package network.beechat.kaonic.communication;

import android.content.ContentResolver;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import network.beechat.kaonic.impl.KaonicLib;
import network.beechat.kaonic.models.KaonicEvent;
import network.beechat.kaonic.models.KaonicEventData;
import network.beechat.kaonic.models.KaonicEventType;
import network.beechat.kaonic.models.ContactFoundEvent;
import network.beechat.kaonic.models.calls.CallEventData;
import network.beechat.kaonic.models.connection.ConnectionConfig;
import network.beechat.kaonic.models.messages.ChatCreateEvent;
import network.beechat.kaonic.models.messages.MessageFileEvent;
import network.beechat.kaonic.models.messages.MessageFileStartEvent;
import network.beechat.kaonic.models.messages.MessageLocationEvent;
import network.beechat.kaonic.models.messages.MessageTextEvent;

public class KaonicCommunicationManager {
    final private String TAG = "LibCommunicationHandler";
    final private @NonNull KaonicLib kaonicLib;
    final private @NonNull ContentResolver contentResolver;
    final private ObjectMapper objectMapper = new ObjectMapper();
    final private Map<String, FileManager> fileReceivers = new HashMap<>();
    final private Map<String, FileManager> fileSenders = new HashMap<>();
    private KaonicEventListener eventListener;
    private String myAddress = "1234567890";


    public KaonicCommunicationManager(@NonNull KaonicLib kaonicLib, @NonNull ContentResolver resolver) {
        this.kaonicLib = kaonicLib;
        this.contentResolver = resolver;
        kaonicLib.setEventListener(new KaonicLib.EventListener() {
            @Override
            public void onEventReceived(String jsonData) {
                kaonicOnEventReceived(jsonData);
            }

            @Override
            public void onFileChunkRequest(String fileId, int chunkSize) {
                kaonicOnFileChunkRequest(fileId, chunkSize);
            }

            @Override
            public void onFileChunkReceived(String fileId, byte[] bytes) {
                kaonicOnFileChunkReceived(fileId, bytes);
            }
        });
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

    public String generateSecret() {
        return kaonicLib.generateSecret();
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

    public void sendConfig(int mcs, int optionNumber, int module, int frequency,
                           int channel, int channelSpacing, int txPower) {
        try {
            JSONObject config = new JSONObject();
            config.put("mcs", mcs);
            config.put("opt", optionNumber);
            config.put("module", module);
            config.put("freq", frequency);
            config.put("channel", channel);
            config.put("channel_spacing", channelSpacing);
            config.put("tx_power", txPower);

            String jsonString = config.toString();
            kaonicLib.sendConfig(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //region Chat methods
    public void createChat(String address, String chatId) {
        try {
            kaonicLib.createChat(objectMapper.writeValueAsString(new KaonicEvent(KaonicEventType.CHAT_CREATE,
                    new ChatCreateEvent(address, chatId))));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String address, String message, String chatId) {
        transmitData(new KaonicEvent(KaonicEventType.MESSAGE_TEXT,
                new MessageTextEvent(address, System.currentTimeMillis(), chatId, message)));
        try {
            kaonicOnEventReceived(objectMapper.writeValueAsString(new KaonicEvent(KaonicEventType.MESSAGE_TEXT,
                    new MessageTextEvent(myAddress, System.currentTimeMillis(), chatId, message))));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendFile(String filePath, String address, String chatId) {
        FileManager fileSender = new FileManager();
        String fileId = UUID.randomUUID().toString();
        try {
            boolean canStart = fileSender.startSend(contentResolver, fileId, chatId, address, filePath);
            if (canStart) {
                fileSenders.put(fileId, fileSender);
                MessageFileStartEvent messageFileEvent = new MessageFileStartEvent(fileSender.getAddress(),
                        System.currentTimeMillis(), fileSender.getFileId(), fileSender.getChatId(),
                        fileSender.getFileName(), fileSender.getFileSize());
                try {
                    KaonicEvent<MessageFileStartEvent> kaonicEvent = new KaonicEvent<>(KaonicEventType.MESSAGE_FILE_START,
                            messageFileEvent);
                    transmitFile(kaonicEvent);

                    kaonicOnEventReceived(objectMapper.writeValueAsString(new KaonicEvent<>(KaonicEventType.MESSAGE_FILE,
                            new MessageFileEvent(myAddress,
                                    System.currentTimeMillis(), fileSender.getFileId(), fileSender.getChatId(),
                                    fileSender.getFileName(), fileSender.getFileSize()))));

                } catch (JsonProcessingException e) {
                    Log.e(TAG, e.toString());
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion

    //region Call methods
    public void sendCallEvent(@NonNull String callEventType, @NonNull String address, @NonNull String callId) {
        try {
            switch (callEventType) {
                case KaonicEventType.CALL_INVOKE:
                    kaonicLib.sendCallInvoke(objectMapper.writeValueAsString(new KaonicEvent(KaonicEventType.CALL_INVOKE,
                            new CallEventData(address, callId))));
                    break;
                case KaonicEventType.CALL_ANSWER:
                    kaonicLib.sendCallAnswer(objectMapper.writeValueAsString(new KaonicEvent(KaonicEventType.CALL_ANSWER,
                            new CallEventData(address, callId))));
                    break;
                case KaonicEventType.CALL_REJECT:
                    kaonicLib.sendCallReject(objectMapper.writeValueAsString(new KaonicEvent(KaonicEventType.CALL_REJECT,
                            new CallEventData(address, callId))));
                    break;

            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion

    private void transmitData(KaonicEvent kaonicEvent) {
        try {
            final String jsonString = objectMapper.writeValueAsString(kaonicEvent);
            kaonicLib.sendMessage(jsonString);
        } catch (JsonProcessingException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            throw new RuntimeException(e);
        }
    }


    private void transmitFile(KaonicEvent kaonicEvent) {
        try {
            String jsonString = objectMapper.writeValueAsString(kaonicEvent);
            Log.i(TAG, "\uD83D\uDD3C Kaonic file sent:" + jsonString);
            kaonicLib.sendFile(jsonString);
        } catch (JsonProcessingException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    private void kaonicOnEventReceived(String dataJson) {
        // Log.i(TAG, "\uD83D\uDD3D Kaonic data received:" + dataJson);
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
                case KaonicEventType.CHAT_CREATE:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), ChatCreateEvent.class);
                    break;
                case KaonicEventType.MESSAGE_TEXT:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), MessageTextEvent.class);
                    break;
                case KaonicEventType.MESSAGE_LOCATION:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), MessageLocationEvent.class);
                    break;
                case KaonicEventType.MESSAGE_FILE_START:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), MessageFileStartEvent.class);
                    startFileReceiving((MessageFileStartEvent) kaonicEventData);
                    return;
                case KaonicEventType.MESSAGE_FILE:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), MessageFileEvent.class);
                    break;
                case KaonicEventType.CALL_INVOKE:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), CallEventData.class);
                    break;
                case KaonicEventType.CALL_ANSWER:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), CallEventData.class);

                    break;
                case KaonicEventType.CALL_REJECT:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), CallEventData.class);
                    kaonicLib.stopAudio();
                    break;
            }
            if (kaonicEventData != null) {
                final KaonicEvent event = new KaonicEvent(eventType);
                event.data = kaonicEventData;

                if (eventListener != null) {
                    eventListener.onEventReceived(event);
                }
            }
        } catch (JSONException | JsonProcessingException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    private void kaonicOnFileChunkRequest(String fileId, int chunkSize) {
        FileManager fileSender = fileSenders.get(fileId);
        if (fileSender == null) return;

        MessageFileEvent messageFileEvent = new MessageFileEvent(myAddress,
                System.currentTimeMillis(), fileSender.getFileId(), fileSender.getChatId(),
                fileSender.getFileName(), fileSender.getFileSize());
        messageFileEvent.path = fileSender.getFileUri().toString();

        try {
            byte[] chunk = fileSender.nextChunk(chunkSize);
            kaonicLib.sendFileChunk(fileSender.getAddress(), fileId, chunk);

            if (fileSender.isFinished()) {
                fileSender.close();
                fileSenders.remove(fileId);
            }

            try {
                messageFileEvent.fileSizeProcessed = fileSender.getProcessedBytes();
                KaonicEvent<MessageFileEvent> kaonicEvent = new KaonicEvent<>(KaonicEventType.MESSAGE_FILE,
                        messageFileEvent);
                kaonicOnEventReceived(objectMapper.writeValueAsString(kaonicEvent));
            } catch (JsonProcessingException e) {
                Log.e(TAG, e.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void kaonicOnFileChunkReceived(String fileId, byte[] bytes) {
        FileManager fileReceiver = fileReceivers.get(fileId);
        if (fileReceiver == null) return;

        boolean fileFinished = fileReceiver.writeChunk(bytes);
        MessageFileEvent messageFileEvent = new MessageFileEvent(fileReceiver.getAddress(),
                System.currentTimeMillis(), fileReceiver.getFileId(), fileReceiver.getChatId(),
                fileReceiver.getFileName(), fileReceiver.getFileSize());
        messageFileEvent.fileSizeProcessed = fileReceiver.getProcessedBytes();
        if (fileFinished) {
            messageFileEvent.path = fileReceiver.getFileUri().getPath();
            fileReceiver.close();
            fileReceivers.remove(fileId);
        }

        try {
            KaonicEvent<MessageFileEvent> kaonicEvent = new KaonicEvent<>(KaonicEventType.MESSAGE_FILE,
                    messageFileEvent);
            kaonicOnEventReceived(objectMapper.writeValueAsString(kaonicEvent));
        } catch (JsonProcessingException e) {
            Log.e(TAG, e.toString());
        }

    }

    private void startFileReceiving(MessageFileStartEvent fileStartEvent) {
        FileManager fileReceiver = new FileManager();
        try {
            fileReceiver.startWrite(contentResolver, fileStartEvent.fileName, fileStartEvent.fileSize,
                    fileStartEvent.fileId, fileStartEvent.chatId, fileReceiver.getAddress());
            fileReceivers.put(fileStartEvent.fileId, fileReceiver);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }
}
