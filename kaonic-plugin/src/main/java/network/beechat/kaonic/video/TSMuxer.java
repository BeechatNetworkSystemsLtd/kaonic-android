
package network.beechat.kaonic.video;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TSMuxer {
    private static final int TS_PACKET_SIZE = 188;
    private static final int PID_VIDEO = 0x100;
    private static final int STREAM_ID_H264 = 0xE0;

    private final AtomicInteger continuityCounter = new AtomicInteger(0);
    private final FrameListener listener;

    public interface FrameListener {
        void onFrameReceived(byte[] tsPacket);
    }

    public TSMuxer(FrameListener listener) {
        this.listener = listener;
    }

    public void muxNALUnit(byte[] nalUnit, long pts, long dts) {
        byte[] pes = createPESPacket(nalUnit, pts, dts);
        List<byte[]> tsPackets = packetize(pes);

        for (byte[] ts : tsPackets) {
            listener.onFrameReceived(ts);
        }
    }

    private byte[] createPESPacket(byte[] nalUnit, long pts, long dts) {
        int pesHeaderLength = 14;
        boolean includeDts = pts != dts;

        if (includeDts) pesHeaderLength += 5;

        int pesPacketLength = pesHeaderLength + nalUnit.length;
        ByteBuffer buffer = ByteBuffer.allocate(pesPacketLength + 6);

        buffer.put((byte) 0x00); // PES start code prefix
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x01);
        buffer.put((byte) STREAM_ID_H264);
        buffer.putShort((short) (pesPacketLength & 0xFFFF));

        buffer.put((byte) 0x80); // '10' for fixed bits + PTS/DTS flags
        buffer.put((byte) (includeDts ? 0xC0 : 0x80)); // PTS only or PTS + DTS
        buffer.put((byte) (pesHeaderLength - 9)); // PES header data length

        writeTimestamp(buffer, 0x02, pts); // PTS
        if (includeDts) {
            writeTimestamp(buffer, 0x01, dts); // DTS
        }

        buffer.put(nalUnit);

        return buffer.array();
    }

    private void writeTimestamp(ByteBuffer buf, int flag, long ts) {
        long val = (flag << 4) |
                (((ts >> 30) & 0x07) << 1) | 1;
        buf.put((byte) val);
        val = (((ts >> 15) & 0x7FFF) << 1) | 1;
        buf.putShort((short) val);
        val = ((ts & 0x7FFF) << 1) | 1;
        buf.putShort((short) val);
    }

    private List<byte[]> packetize(byte[] pes) {
        List<byte[]> packets = new ArrayList<>();

        int offset = 0;
        boolean firstPacket = true;

        while (offset < pes.length) {
            int payloadSize = TS_PACKET_SIZE - 4;
            byte[] tsPacket = new byte[TS_PACKET_SIZE];

            tsPacket[0] = 0x47;
            tsPacket[1] = (byte) ((firstPacket ? 0x40 : 0x00) | ((PID_VIDEO >> 8) & 0x1F));
            tsPacket[2] = (byte) (PID_VIDEO & 0xFF);
            tsPacket[3] = (byte) (0x10 | (continuityCounter.getAndIncrement() & 0x0F));

            int dataRemaining = pes.length - offset;
            int bytesToCopy = Math.min(dataRemaining, payloadSize);

            System.arraycopy(pes, offset, tsPacket, 4, bytesToCopy);

            if (bytesToCopy < payloadSize) {
                for (int i = 4 + bytesToCopy; i < TS_PACKET_SIZE; i++) {
                    tsPacket[i] = (byte) 0xFF;
                }
            }

            offset += bytesToCopy;
            firstPacket = false;
            packets.add(tsPacket);
        }

        return packets;
    }
}
