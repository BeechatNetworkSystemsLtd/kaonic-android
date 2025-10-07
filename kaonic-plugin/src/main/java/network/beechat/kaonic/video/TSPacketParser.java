package network.beechat.kaonic.video;

import java.util.Arrays;

public class TSPacketParser {
    private static final int TS_PACKET_SIZE = 188;
    private static final int SYNC_BYTE = 0x47;
    private static final int VIDEO_PID = 256; // same as in your TSMuxer

    private boolean expectingStart = true;
    private ByteArrayBuffer pesBuffer = new ByteArrayBuffer();

    public byte[] parsePacket(byte[] packet) {
        if ((packet[0] & 0xFF) != SYNC_BYTE) return null;

        int pid = ((packet[1] & 0x1F) << 8) | (packet[2] & 0xFF);
        boolean payloadUnitStart = (packet[1] & 0x40) != 0;

        if (pid != VIDEO_PID) return null;

        int adaptationFieldControl = (packet[3] >> 4) & 0x03;
        int payloadStart = 4;

        if ((adaptationFieldControl & 0x2) != 0) {
            int adaptationFieldLength = packet[4] & 0xFF;
            payloadStart += 1 + adaptationFieldLength;
        }

        if (payloadStart >= TS_PACKET_SIZE) return null;

        if (payloadUnitStart) {
            if (pesBuffer.size() > 0) {
                byte[] nal = pesBuffer.toByteArray();
                pesBuffer.clear();
                pesBuffer.append(packet, payloadStart, TS_PACKET_SIZE - payloadStart);
                return extractNALUnit(nal);
            }
        }

        pesBuffer.append(packet, payloadStart, TS_PACKET_SIZE - payloadStart);
        return null;
    }

    private byte[] extractNALUnit(byte[] pesPayload) {
        int offset = 9 + ((pesPayload[8] & 0xFF)); // skip PES header
        if (offset >= pesPayload.length) return null;
        byte[] nal = Arrays.copyOfRange(pesPayload, offset, pesPayload.length);
        return nal;
    }
}