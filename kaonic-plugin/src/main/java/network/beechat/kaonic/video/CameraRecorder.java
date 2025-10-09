package network.beechat.kaonic.video;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Size;
import android.view.Surface;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

public class CameraRecorder {
    private final Context context;
    private final int width = 640;
    private final int height = 480;
    private final int framerate = 30;

    private MediaCodec encoder;
    private final Executor executor;
    private final VideoStreamListener videoStreamListener;
    private String address;
    private String callId;
    private TSMuxer tsMuxerOld;

    public CameraRecorder(Context context, VideoStreamListener videoStreamListener) {
        this.context = context;
        this.executor = ContextCompat.getMainExecutor(context);
        this.videoStreamListener = videoStreamListener;
        tsMuxerOld = new TSMuxer(tsPacket -> videoStreamListener.onFrameReceived(address, callId, tsPacket));
    }

    public void startRecording(String address, String callId) {
        this.address = address;
        this.callId = callId;

        try {
            initEncoder();

            startCamera();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initEncoder() throws IOException {
        encoder = MediaCodec.createEncoderByType("video/avc");

        MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 2_000_000);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoder.start();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(width, height))
                        .setTargetRotation(Surface.ROTATION_270)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(executor, image -> {
                    encodeImage(image);
                    image.close();
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        (LifecycleOwner) context,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        analysis
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    private void encodeImage(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        if (planes.length < 3) return;

        // Get the image rotation
        int rotation = image.getImageInfo().getRotationDegrees();
        
        byte[] nv21 = yuv420ToNV21(planes);
        
        // If image is rotated 90 or 270 degrees, we might need to handle it
        // For now, let's try with the corrected camera configuration
        
        int inputBufferIndex = encoder.dequeueInputBuffer(0);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = encoder.getInputBuffer(inputBufferIndex);
            if (inputBuffer != null) {
                inputBuffer.clear();
                inputBuffer.put(nv21);
                long pts = System.nanoTime() / 1000;
                encoder.queueInputBuffer(inputBufferIndex, 0, nv21.length, pts, 0);
            }
        }

        drainEncoder();
    }

    private void drainEncoder() {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (true) {
            int outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 0);
            if (outputIndex >= 0) {
                ByteBuffer outBuffer = encoder.getOutputBuffer(outputIndex);
                if (outBuffer != null && bufferInfo.size > 0) {
                    byte[] data = new byte[bufferInfo.size];
                    outBuffer.get(data);
                    long pts = (System.nanoTime() / 1000) * 90 / 1000;
                    long dts = pts;
                    tsMuxerOld.muxNALUnit(data, pts, dts);
                }
                encoder.releaseOutputBuffer(outputIndex, false);
            } else {
                break;
            }
        }
    }

    private byte[] yuv420ToNV21(ImageProxy.PlaneProxy[] planes) {
        byte[] out = new byte[width * height * 3 / 2];

        ByteBuffer yBuffer = planes[0].getBuffer(); // Y
        ByteBuffer uBuffer = planes[1].getBuffer(); // U
        ByteBuffer vBuffer = planes[2].getBuffer(); // V

        int yRowStride = planes[0].getRowStride();
        int uvRowStride = planes[1].getRowStride();
        int uvPixelStride = planes[1].getPixelStride();

        int pos = 0;

        // Copy Y plane
        for (int row = 0; row < height; row++) {
            yBuffer.position(row * yRowStride);
            yBuffer.get(out, pos, width);
            pos += width;
        }

        // Copy interleaved UV (NV21)
        int chromaHeight = height / 2;
        int chromaWidth = width / 2;

        for (int row = 0; row < chromaHeight; row++) {
            int uvRowStart = row * uvRowStride;
            for (int col = 0; col < chromaWidth; col++) {
                int uvOffset = uvRowStart + col * uvPixelStride;
                out[pos++] = uBuffer.get(uvOffset); // U
                out[pos++] = vBuffer.get(uvOffset); // V
            }
        }

        return out;
    }

    public void stopRecording() {
        if (encoder != null) {
            encoder.stop();
            encoder.release();
        }
        address = "";
        callId = "";
    }
}