package network.beechat.kaonic.chat;

import android.content.ContentResolver;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

import network.beechat.kaonic.FileWriteHelper;
import network.beechat.kaonic.communication.DataPacketListener;
import network.beechat.kaonic.communication.LibCommunicationHandler;
import network.beechat.kaonic.models.datapacket.DataPacket;
import network.beechat.kaonic.models.FileMessage;
import network.beechat.kaonic.models.LocationMessage;
import network.beechat.kaonic.models.Message;
import network.beechat.kaonic.models.Node;
import network.beechat.kaonic.models.TextMessage;
import network.beechat.kaonic.models.datapacket.DataPacketType;
import network.beechat.kaonic.models.datapacket.messages.FileChunkMessagePacket;
import network.beechat.kaonic.models.datapacket.messages.FileStartMessagePacket;
import network.beechat.kaonic.models.datapacket.messages.LocationMessagePacket;
import network.beechat.kaonic.models.datapacket.messages.TextMessagePacket;

public class ChatService implements DataPacketListener {
    private final Node me;
    private final ContentResolver contentResolver;
    private final LibCommunicationHandler communicationHandler;
    // chatCallback for all chats with all users
    private ChatCallback chatCallback;
    private Map<String, FileWriteHelper> fileHelpers = new HashMap<>();

    public ChatService(Node me, ContentResolver contentResolver, LibCommunicationHandler communicationHandler) {
        this.me = me;
        this.communicationHandler = communicationHandler;
        this.contentResolver = contentResolver;
        communicationHandler.setPacketListener(this);
    }

    public void initializeService(ChatCallback chatCallback) {
        this.chatCallback = chatCallback;
        //smth with kaonic
    }

    public void sendTextMessage(String chatUuid, String message, Node recipient) {
        Message textMessage = new TextMessage(chatUuid, message, me, recipient);
        sendMessage(textMessage);
    }

    public void sentFileMessage(String chatUuid, String pathToFile, Node recipient) {
        String filename = pathToFile.substring(pathToFile.lastIndexOf("/") + 1);
        Message fileMessage = new FileMessage(chatUuid, filename, 0, me, recipient);
        sendMessage(fileMessage);
    }

    public void sentLocationMessage(String chatUuid, float latitude, float longitude, Node recipient) {
        Message locationMessage = new LocationMessage(chatUuid, latitude, longitude, me, recipient);
        sendMessage(locationMessage);
    }

    @Override
    public void onPacket(DataPacket packet, Node sender) {
        if (packet == null) return;

        switch (packet.type) {
            case DataPacketType.TEXT_MESSAGE:
                onMessageReceivedCallback(TextMessage.fromPacket((TextMessagePacket) packet,
                        sender, me));
                break;
            case DataPacketType.LOCATION_MESSAGE:
                onMessageReceivedCallback(LocationMessage.fromPacket((LocationMessagePacket) packet,
                        sender, me));
                break;
            case DataPacketType.FILE_START_MESSAGE:
                handleMessageStart((FileStartMessagePacket) packet, sender);
                break;
            case DataPacketType.FILE_CHUNK_MESSAGE:
                handleFileChunk((FileChunkMessagePacket) packet, sender);
                break;
        }
    }

    private void handleMessageStart(FileStartMessagePacket filePacket, Node sender) {
        FileMessage fileMessage = FileMessage.fromStartPacket(filePacket,
                sender, me);
        FileWriteHelper fileHelper = new FileWriteHelper();
        fileHelper.open(contentResolver, fileMessage.fileName, fileMessage.fileSize);
        fileHelpers.put(fileMessage.uuid, fileHelper);

        //start write file
        onMessageReceivedCallback(fileMessage);
    }

    private void handleFileChunk(FileChunkMessagePacket chunkPacket, Node sender) {
        FileWriteHelper fileWriteHelper = fileHelpers.get(chunkPacket.id);
        if (fileWriteHelper == null) return;

        /// write chunk to file
        /// check if that's the last part
        boolean finished = fileWriteHelper.writeChunk(chunkPacket.bytes);
        FileMessage fileMessage;
        Uri fileUri = fileWriteHelper.getFileUri();
        if (finished) {
            fileWriteHelper.close();
            fileHelpers.remove(chunkPacket.id);
        }

        if (fileUri == null) return;
        final String pathToFile = fileUri.getPath();
        final String fileName = pathToFile.substring(pathToFile.lastIndexOf("/") + 1);
        fileMessage = new FileMessage(chunkPacket.id, chunkPacket.chatUuid,
                fileName, fileWriteHelper.getFileSize(), fileName, sender, me);

        if (chatCallback != null) {
            chatCallback.onMessageUpdated(fileMessage);
        }
    }

    private void sendMessage(Message message) {
        // kaonic send command
        // communicationHandler.transmitData();

        if (chatCallback != null) {
            chatCallback.onMessageSent(message);
        }
    }

    private void onMessageReceivedCallback(Message message) {
        if (chatCallback != null) {
            chatCallback.onMessageReceived(message);
        }
    }

}
