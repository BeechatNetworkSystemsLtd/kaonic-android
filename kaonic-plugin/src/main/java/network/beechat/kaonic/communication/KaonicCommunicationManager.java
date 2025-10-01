package network.beechat.kaonic.communication;

import android.content.ContentResolver;
import android.media.Ringtone;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import network.beechat.kaonic.audio.AudioStreamCallback;
import network.beechat.kaonic.communication.base.KaonicBaseManager;
import network.beechat.kaonic.impl.KaonicLib;
import network.beechat.kaonic.models.BroadcastEventData;
import network.beechat.kaonic.models.KaonicEvent;
import network.beechat.kaonic.models.KaonicEventData;
import network.beechat.kaonic.models.KaonicEventType;
import network.beechat.kaonic.models.ContactFoundEvent;
import network.beechat.kaonic.models.calls.CallEventData;
import network.beechat.kaonic.models.messages.ChatCreateEvent;
import network.beechat.kaonic.models.messages.MessageFileEvent;
import network.beechat.kaonic.models.messages.MessageFileStartEvent;
import network.beechat.kaonic.models.messages.MessageLocationEvent;
import network.beechat.kaonic.models.messages.MessageTextEvent;

@Keep
public class KaonicCommunicationManager extends KaonicBaseManager {
    final private String TAG = "LibCommunicationHandler";
    final private @NonNull ContentResolver contentResolver;
    final private Map<String, FileManager> fileReceivers = new HashMap<>();
    final private Map<String, FileManager> fileSenders = new HashMap<>();
    private KaonicEventListener eventListener;
    private CallHandler callHandler = new CallHandler();
    private String myAddress = "1234567890";
    private AudioStreamCallback audioStreamCallback = (size, buffer) ->
            sendCallData(callHandler.getActiveCallAddress(),
                    callHandler.getActiveCallId(), buffer);


    public KaonicCommunicationManager(@NonNull KaonicLib kaonicLib, @NonNull ContentResolver resolver,
                                      @NonNull Ringtone ringtone) {
        super(kaonicLib);
        this.contentResolver = resolver;
        callHandler.initHandler(audioStreamCallback, ringtone);

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
            public void onBroadcastReceived(@NonNull String address, @NonNull String id,
                                            @NonNull String topic, @NonNull byte[] bytes) {
                kaonicOnBroadcastReceived(address, id, topic, bytes);
            }

            @Override
            public void onFileChunkReceived(String fileId, byte[] bytes) {
                kaonicOnFileChunkReceived(fileId, bytes);
            }

            @Override
            public void onAudioChunkReceived(String address, String callId, byte[] buffer) {
                kaonicOnAudioChunkReceived(address, callId, buffer);
            }

            @Override
            public void onVideoChunkReceived(String address, String callId, byte[] buffer) {
                // TODO: Add video management
            }
        });
    }

    @Keep
    public void onDestroy() {
        kaonicLib.removeChannelListener();
    }

    public void setEventListener(KaonicEventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Keep
    public void removePacketListener() {
        this.eventListener = null;
    }

    public String getMyAddress() {
        return myAddress;
    }

    public void sendConfig(String  jsonConfig) {
        kaonicLib.sendConfig(jsonConfig);
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
            KaonicEvent event = new KaonicEvent(callEventType,
                    new CallEventData(address, callId));
            switch (callEventType) {
                case KaonicEventType.CALL_INVOKE:
                case KaonicEventType.CALL_ANSWER:
                case KaonicEventType.CALL_REJECT:
                    kaonicLib.sendCallEvent(objectMapper.writeValueAsString(event));
                    callHandler.onCallEventReceived(event);

            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendCallData(String address, String callId, byte[] buffer) {
        kaonicLib.sendCallAudio(address, callId, buffer);
    }
    //endregion

    public void sendBroadcast(String id, String topic, byte[] data) {
        kaonicLib.sendBroadcast(id, topic, data);
    }

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
                case KaonicEventType.CALL_ANSWER:
                case KaonicEventType.CALL_REJECT:
                    kaonicEventData = objectMapper.readValue(eventData.toString(), CallEventData.class);
                    callHandler.onCallEventReceived(new KaonicEvent(eventType, kaonicEventData));
                    break;
            }
            if (kaonicEventData != null) {
                final KaonicEvent event = new KaonicEvent(eventType, kaonicEventData);

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

    private void kaonicOnBroadcastReceived(@NonNull String address, @NonNull String id,
                                           @NonNull String topic, @NonNull byte[] bytes) {
        Log.i(TAG, "OnBroadcastReceived " + address + " " + id + " " + topic + " " + Arrays.toString(bytes));
        KaonicEvent<BroadcastEventData> kaonicEvent = new KaonicEvent<>(KaonicEventType.BROADCAST,
                new BroadcastEventData(address, id, topic, bytes));
        if (eventListener != null) {
            eventListener.onEventReceived(kaonicEvent);
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

    private void kaonicOnAudioChunkReceived(String address, String callId, byte[] buffer) {
        callHandler.play(buffer, buffer.length);
    }
}
