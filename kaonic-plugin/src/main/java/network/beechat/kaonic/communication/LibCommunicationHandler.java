package network.beechat.kaonic.communication;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

import network.beechat.kaonic.libsource.KaonicDataChannelListener;
import network.beechat.kaonic.libsource.KaonicLib;
import network.beechat.kaonic.models.Node;
import network.beechat.kaonic.models.datapacket.DataPacket;
import network.beechat.kaonic.models.datapacket.DataPacketType;
import network.beechat.kaonic.models.datapacket.messages.FileChunkMessagePacket;
import network.beechat.kaonic.models.datapacket.messages.FileStartMessagePacket;
import network.beechat.kaonic.models.datapacket.messages.LocationMessagePacket;
import network.beechat.kaonic.models.datapacket.messages.TextMessagePacket;

public class LibCommunicationHandler implements KaonicDataChannelListener {

    final private @NonNull KaonicLib kaonicLib;
    private DataPacketListener packetListener;

    public LibCommunicationHandler(@NonNull KaonicLib kaonicLib) {
        this.kaonicLib = kaonicLib;
        kaonicLib.setChannelListener(this);
    }

    public void onDestroy() {
        kaonicLib.removeChannelListener();
    }

    public void setPacketListener(DataPacketListener packetListener) {
        this.packetListener = packetListener;
    }

    public void removePacketListener() {
        this.packetListener = null;
    }

    public void transmitData(String address, DataPacket dataPacket) {
        byte[] cborData;
        try {
            cborData = CborParser.getInstance().toBytes(dataPacket);
        } catch (JsonProcessingException e) {
            cborData = new byte[0];
        }

        if (cborData.length == 0) return;

        kaonicLib.transmit(address, cborData);
    }

    @Override
    public void onDataReceive(@NonNull byte[] bytes, String address) {
        if (bytes.length < 1) return;
        int packetType = bytes[1];
        DataPacket packet = null;
        try {
            switch (packetType) {
                case DataPacketType.TEXT_MESSAGE:
                    packet = CborParser.getInstance().toObj(bytes, TextMessagePacket.class);
                    break;
                case DataPacketType.LOCATION_MESSAGE:
                    packet = CborParser.getInstance().toObj(bytes, LocationMessagePacket.class);
                    break;
                case DataPacketType.FILE_START_MESSAGE:
                    packet = CborParser.getInstance().toObj(bytes, FileStartMessagePacket.class);
                    break;
                case DataPacketType.FILE_CHUNK_MESSAGE:
                    packet = CborParser.getInstance().toObj(bytes, FileChunkMessagePacket.class);
                    break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (packet != null) {
            packetListener.onPacket(packet, new Node(address));
        }
    }
}
