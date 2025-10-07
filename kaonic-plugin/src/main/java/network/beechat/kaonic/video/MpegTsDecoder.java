package network.beechat.kaonic.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MpegTsDecoder {
    private static final String TAG = "MpegTsDecoder";
    private static final int TS_PACKET_SIZE = 188;

    private final MediaCodec decoder;
    private final TSPacketParser tsParser;
    private final List<Byte> nalBuffer = new ArrayList<>();

    public MpegTsDecoder(Surface outputSurface, int width, int height) throws IOException {
        decoder = MediaCodec.createDecoderByType("video/avc");

        MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
        decoder.configure(format, outputSurface, null, 0);
        decoder.start();

        tsParser = new TSPacketParser();
    }

    public void onPacketReceived(byte[] tsPacket) {
        if (tsPacket.length != TS_PACKET_SIZE) return;

        byte[] nalUnit = tsParser.parsePacket(tsPacket);
        if (nalUnit != null) {
            decodeNAL(nalUnit);
        }

        drainDecoder();
    }

    private void decodeNAL(byte[] nal) {
        int inIndex = decoder.dequeueInputBuffer(0);
        if (inIndex >= 0) {
            ByteBuffer inBuf = decoder.getInputBuffer(inIndex);
            if (inBuf != null) {
                inBuf.clear();
                inBuf.put(nal);
                decoder.queueInputBuffer(inIndex, 0, nal.length, System.nanoTime() / 1000, 0);
            }
        }
    }

    private void drainDecoder() {
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        while (true) {
            int outIndex = decoder.dequeueOutputBuffer(info, 0);
            if (outIndex >= 0) {
                decoder.releaseOutputBuffer(outIndex, true); // render to surface
            } else {
                break;
            }
        }
    }

    public void stop() {
        decoder.stop();
        decoder.release();
    }
}