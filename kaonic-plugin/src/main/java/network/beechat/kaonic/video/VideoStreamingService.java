package network.beechat.kaonic.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VideoStreamingService {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;
    private static final int BITRATE = 2_000_000;
    private static final int FRAMERATE = 30;
    private static final int IFRAME_INTERVAL = 1; // in seconds
    private static final int MAX_CHUNK_SIZE = 1024;
    private static final int TS_PACKET_SIZE = 188;

    private MediaCodec encoder;
    private Surface inputSurface;
    private boolean isStreaming = false;

    public interface ChunkSender {
        void sendChunk(byte[] chunk);
    }

    private final ChunkSender sender;
    private final TsMuxer tsMuxer = new TsMuxer();

    public VideoStreamingService(ChunkSender sender) {
        this.sender = sender;
    }

    public Surface start() throws Exception {
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", WIDTH, HEIGHT);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BITRATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAMERATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        encoder = MediaCodec.createEncoderByType("video/avc");
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        inputSurface = encoder.createInputSurface();
        encoder.start();

        isStreaming = true;
        new Thread(this::encodeLoop).start();

        return inputSurface;
    }

    public void stop() {
        isStreaming = false;
        if (encoder != null) {
            encoder.stop();
            encoder.release();
        }
    }

    private void encodeLoop() {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        while (isStreaming) {
            int outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000);
            if (outputIndex >= 0) {
                ByteBuffer encodedData = encoder.getOutputBuffer(outputIndex);
                if (encodedData != null && bufferInfo.size > 0) {
                    byte[] frame = new byte[bufferInfo.size];
                    encodedData.position(bufferInfo.offset);
                    encodedData.get(frame);

                    List<byte[]> tsPackets = tsMuxer.mux(frame, bufferInfo.presentationTimeUs);
                    sendPacketsAsChunks(tsPackets);
                }
                encoder.releaseOutputBuffer(outputIndex, false);
            }
        }
    }

    private void sendPacketsAsChunks(List<byte[]> tsPackets) {
        ByteArrayOutputStream chunk = new ByteArrayOutputStream();
        for (byte[] pkt : tsPackets) {
            if (chunk.size() + TS_PACKET_SIZE > MAX_CHUNK_SIZE) {
                sender.sendChunk(chunk.toByteArray());
                chunk.reset();
            }
            chunk.write(pkt, 0, pkt.length);
        }
        if (chunk.size() > 0) {
            sender.sendChunk(chunk.toByteArray());
        }
    }

    // Placeholder for a real TS muxer
    private static class TsMuxer {
        public List<byte[]> mux(byte[] h264Frame, long ptsUs) {
            // Minimal stub: Wrap into fake TS packets of 188 bytes
            List<byte[]> packets = new ArrayList<>();
            int i = 0;
            while (i < h264Frame.length) {
                byte[] packet = new byte[TS_PACKET_SIZE];
                packet[0] = 0x47; // Sync byte
                int len = Math.min(TS_PACKET_SIZE - 1, h264Frame.length - i);
                System.arraycopy(h264Frame, i, packet, 1, len);
                packets.add(packet);
                i += len;
            }
            return packets;
        }
    }
}
