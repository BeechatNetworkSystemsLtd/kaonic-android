package network.beechat.kaonic.video;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import org.freedesktop.gstreamer.GStreamer;

import network.beechat.kaonic.R;

public class VideoStreamingActivity extends Activity implements SurfaceHolder.Callback{
    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativeSetUri(String uri); // Set the URI of the media to play
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativeSetPosition(int milliseconds); // Seek to the indicated position, in milliseconds
    private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private native void nativeSurfaceInit(Object surface); // A new surface is available
    private native void nativeSurfaceFinalize(); // Surface about to be destroyed
    private long native_custom_data;      // Native code will use this to keep private data

    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING
    private int position;                 // Current position, reported by native code
    private int duration;                 // Current clip duration, reported by native code
    private int desired_position;         // Position where the users wants to seek to

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Initialize GStreamer and warn if it fails
        try {
            GStreamer.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.video_streaming_activity);

        SurfaceView sv = this.findViewById(R.id.surface_video);
        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(this);

        // Retrieve our previous state, or initialize it to default values
        if (savedInstanceState != null) {
            is_playing_desired = savedInstanceState.getBoolean("playing");
            position = savedInstanceState.getInt("position");
            duration = savedInstanceState.getInt("duration");
            Log.i ("GStreamer", "Activity created with saved state:");
        } else {
            is_playing_desired = true;
            position = duration = 0;
            Log.i ("GStreamer", "Activity created with no saved state:");
        }
        Log.i ("GStreamer", "  playing:" + is_playing_desired + " position:" + position +
                " duration: " + duration);

        nativeInit();
    }

    protected void onSaveInstanceState (Bundle outState) {
        Log.d ("GStreamer", "Saving state, playing:" + is_playing_desired + " position:" + position +
                " duration: " + duration );
        outState.putBoolean("playing", is_playing_desired);
        outState.putInt("position", position);
        outState.putInt("duration", duration);
    }

    protected void onDestroy() {
        nativeFinalize();
        super.onDestroy();
    }

    // Called from native code
    private void setCurrentPosition(final int position, final int duration) {

    }

    // Called from native code. This sets the content of the TextView from the UI thread.
    private void setMessage(final String message) {
        final TextView tv = this.findViewById(R.id.textview_message);
        runOnUiThread(() -> tv.setText(message));
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready t o accept commands.
    private void onGStreamerInitialized () {
        Log.i ("GStreamer", "GStreamer initialized:");
        Log.i ("GStreamer", "  playing:" + is_playing_desired + " position:" + position);

        // Restore previous playing state
        nativeSetPosition (position);
        if (is_playing_desired) {
            nativePlay();
        } else {
            nativePause();
        }
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("kaonic-streamer");
        nativeClassInit();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        Log.d("GStreamer", "Surface changed to format " + format + " width "
                + width + " height " + height);
        nativeSurfaceInit (holder.getSurface());
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface created: " + holder.getSurface());
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface destroyed");
        nativeSurfaceFinalize ();
    }

    // Called from native code when the size of the media changes or is first detected.
    // Inform the video surface about the new size and recalculate the layout.
    private void onMediaSizeChanged (int width, int height) {
        Log.i ("GStreamer", "Media size changed to " + width + "x" + height);
        final GStreamerSurfaceView gsv = this.findViewById(R.id.surface_video);
        gsv.media_width = width;
        gsv.media_height = height;
        runOnUiThread(gsv::requestLayout);
    }

    // The user started dragging the Seek Bar thumb
    public void onStartTrackingTouch(SeekBar sb) {
        nativePause();
    }

    // The user released the Seek Bar thumb
    public void onStopTrackingTouch(SeekBar sb) {
        // If this is a remote file, scrub seeking is probably not going to work smoothly enough.
        // Therefore, perform only the seek when the slider is released.
        if (is_playing_desired) nativePlay();
    }
}
