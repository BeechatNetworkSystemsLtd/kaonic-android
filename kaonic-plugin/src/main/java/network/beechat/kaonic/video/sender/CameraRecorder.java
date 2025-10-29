package network.beechat.kaonic.video.sender;

import android.content.Context;
import android.view.Surface;
import android.widget.Toast;

import network.beechat.kaonic.video.VideoStreamListener;

public class CameraRecorder implements LocalPipelineManager.ByteListener {
    private final VideoStreamListener videoStreamListener;
    private final LocalPipelineManager localPipelineManager;
    private GstAhc gstAhc;
    private GstAhc.State gstState;
    private String address;
    private String callId;


    public CameraRecorder(Context context, VideoStreamListener videoStreamListener,
                          int cameraRotation) {
        this.videoStreamListener = videoStreamListener;
        localPipelineManager = new LocalPipelineManager(this);
        try {
            initGst(GstAhc.init(context),cameraRotation);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void startRecording(String address, String callId) {
        this.address = address;
        this.callId = callId;

        try {
            if (gstState != GstAhc.State.PLAYING) {
                // TODO: need to change to open/close whole pipeline
                gstAhc.togglePlay();
                localPipelineManager.startListening();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void stopRecording() {
        if (gstState == GstAhc.State.PLAYING) {
            // TODO: need to change to open/close whole pipeline
            gstAhc.togglePlay();
        }
        localPipelineManager.stopListening();
        address = "";
        callId = "";
    }

    private void initGst(GstAhc gstAhc, int rotation) {
        this.gstAhc = gstAhc;
        setOrientation (rotation);

        gstAhc.setStateChangedListener((gstAhc1, state) -> {
            gstState = state;
            if (state == GstAhc.State.PLAYING &&
                    (address == null || address.isEmpty())) {
                gstAhc.togglePlay();
            }
        });
    }

    @Override
    public void onBytesReceived(byte[] data, int length) {
        if (address == null || address.isEmpty()) return;

        videoStreamListener.onFrameReceived(address, callId, data);
    }

    private void setOrientation (int rotation)
    {
        GstAhc.Rotate rotate = GstAhc.Rotate.NONE;

        switch (rotation) {
            case Surface.ROTATION_0: rotate = GstAhc.Rotate.CLOCKWISE; break;
            case Surface.ROTATION_90: rotate = GstAhc.Rotate.ROTATE_180; break;
            case Surface.ROTATION_180: rotate = GstAhc.Rotate.NONE; break;
            case Surface.ROTATION_270: rotate = GstAhc.Rotate.NONE; break;
        }

        gstAhc.setRotateMethod(rotate);
    }
}