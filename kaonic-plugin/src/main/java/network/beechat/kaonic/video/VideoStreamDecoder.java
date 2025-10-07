package network.beechat.kaonic.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoStreamDecoder {
    private MediaCodec decoder;
    private boolean processingStream = false;
    private final int width = 640;
    private final int height = 480;
    private final int framerate = 30;

    public void startDecode(Surface outputSurface) {
        stopDecode();
        try {
            decoder = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
        decoder.configure(format, outputSurface, null, 0);
        decoder.start();
        processingStream = true;
    }

    public void stopDecode() {
        try {
            decoder.stop();
        } catch (Exception e) {
            Log.w("Decoder", "stop() failed: " + e);
        }

        try {
            decoder.release();
        } catch (Exception e) {
            Log.w("Decoder", "release() failed: " + e);
        }
        processingStream = false;
        decoder = null;
    }


    public void decodeFrame(byte[] data) {

        int inIndex = decoder.dequeueInputBuffer(10000);
        if (inIndex >= 0) {
            ByteBuffer inputBuffer = decoder.getInputBuffer(inIndex);
            if (inputBuffer != null) {
                inputBuffer.clear();
                inputBuffer.put(data);
                decoder.queueInputBuffer(inIndex, 0, data.length, System.nanoTime() / 1000, 0);
            }
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000);
        if (outIndex >= 0) {
            decoder.releaseOutputBuffer(outIndex, true); // show on surface
        } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            Log.d("Decoder", "Output format changed");
        }

//        int inIndex = decoder.dequeueInputBuffer(10000);
//        if (inIndex >= 0) {
//            ByteBuffer inputBuffer = decoder.getInputBuffer(inIndex);
//            if (inputBuffer != null) {
//                inputBuffer.clear();
//                inputBuffer.put(data);
//
//                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
//                decoder.queueInputBuffer(inIndex, 0, data.length, System.nanoTime() / 1000, info.flags);
//            }
//        }
//
//        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//        int outIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000);
//        if (outIndex >= 0) {
//            decoder.releaseOutputBuffer(outIndex, true); // ✅ render to Surface
//        } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//            Log.d("Decoder", "Output format changed: " + decoder.getOutputFormat());
//        }
    }
}
