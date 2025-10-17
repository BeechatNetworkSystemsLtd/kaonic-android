package network.beechat.kaonic.video.sender;

import android.content.Context;
import android.widget.Toast;

import network.beechat.kaonic.video.VideoStreamListener;

public class CameraRecorder implements LocalPipelineManager.ByteListener {
    private final VideoStreamListener videoStreamListener;
    private final LocalPipelineManager localPipelineManager;
    private GstAhc gstAhc;
    private GstAhc.State gstState;
    private String address;
    private String callId;


    public CameraRecorder(Context context, VideoStreamListener videoStreamListener) {
        this.videoStreamListener = videoStreamListener;
        localPipelineManager = new LocalPipelineManager(this);
        try {
            initGst(GstAhc.init(context));
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

    private void initGst(GstAhc gstAhc) {
        this.gstAhc = gstAhc;
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
}