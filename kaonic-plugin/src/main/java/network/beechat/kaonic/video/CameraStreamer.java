package network.beechat.kaonic.video;

import android.content.Context;
import android.util.Size;
import android.view.Surface;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

public class CameraStreamer {

    private final Context context;
    private final VideoStreamingService videoService;

    private ProcessCameraProvider cameraProvider;
    private Camera camera;

    public CameraStreamer(Context context, VideoStreamingService.ChunkSender sender) {
        this.context = context;
        this.videoService = new VideoStreamingService(sender);
    }

    public void start(String address, String callId) {
        // Stop any existing streaming first
//        stop();
        
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(context);

        future.addListener(() -> {
            try {
                cameraProvider = future.get();

                // Unbind all use cases first to avoid conflicts
                cameraProvider.unbindAll();

                Preview encodingTarget = new Preview.Builder()
                    .build();

                encodingTarget.setSurfaceProvider(request -> {

                    // Use a different executor - sometimes main executor has issues
                    java.util.concurrent.Executor executor =  ContextCompat.getMainExecutor(context);

                    Surface encoderSurface = null;
                    try {
                        encoderSurface = videoService.start(address, callId);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    if (encoderSurface == null) {
                        System.err.println("Failed to create encoder surface");
                        return;
                    }
                    request.provideSurface(encoderSurface, executor, result -> {

                        if (result.getResultCode() == androidx.camera.core.SurfaceRequest.Result.RESULT_SURFACE_USED_SUCCESSFULLY) {
                            System.out.println("Surface connected successfully");
                        } else {
                            System.err.println("Failed to connect surface: " + result.getResultCode());
                            // Clean up on surface failure
                            stop();
                        }
                    });
                    
                    System.out.println("provideSurface() call completed");
                });

                try {
                    camera = cameraProvider.bindToLifecycle(
                            (LifecycleOwner) context,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            encodingTarget
                    );
                    System.out.println("Camera bound successfully");
                } catch (Exception bindException) {
                    System.err.println("Failed to bind camera: " + bindException.getMessage());
                    stop();
                }

            } catch (Exception e) {
                System.err.println("CameraStreamer start failed: " + e.getMessage());
                e.printStackTrace();
                stop();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void stop() {
        try {
            if (cameraProvider != null) {
                cameraProvider.unbindAll();
                cameraProvider = null;
            }
            camera = null;
        } catch (Exception e) {
            System.err.println("Error stopping camera: " + e.getMessage());
        }
        
        try {
            videoService.stop();
        } catch (Exception e) {
            System.err.println("Error stopping video service: " + e.getMessage());
        }
    }
}