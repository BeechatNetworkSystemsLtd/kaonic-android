package network.beechat.kaonic.video.sender;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class LocalPipelineManager {
    private static final String TAG = "LocalPipelineManager";

    private final String host="127.0.0.1";
    private Thread socketThread;
    private volatile boolean isRunning = false;

    public interface ByteListener {
        void onBytesReceived(byte[] data, int length);
    }

    private final ByteListener listener;

    public LocalPipelineManager(ByteListener listener) {
        this.listener = listener;
    }

    public void startListening() {
        if (isRunning) {
            Log.w(TAG, "Already running");
            return;
        }

        isRunning = true;

        socketThread = new Thread(() -> {
            try (Socket socket = new Socket(host, 5005);
                 InputStream inputStream = socket.getInputStream()) {

                Log.i(TAG, "Connected to pipeline at " + host + ":" + "5005");

                byte[] buffer = new byte[4096];
                int bytesRead;

                while (isRunning && (bytesRead = inputStream.read(buffer)) != -1) {
//                    if (listener != null && bytesRead > 0) {
//                        listener.onBytesReceived(buffer, bytesRead);
//                    }
                    int offset = 0;
                    while (offset < bytesRead) {
                        int chunkSize = Math.min(1024, bytesRead - offset);
                        byte[] chunk = new byte[chunkSize];
                        System.arraycopy(buffer, offset, chunk, 0, chunkSize);
                        listener.onBytesReceived(chunk, chunkSize);
                        offset += chunkSize;
                    }
                }

                Log.i(TAG, "Stopped receiving bytes");

            } catch (IOException e) {
                Log.e(TAG, "Socket error: " + e.getMessage(), e);
            } finally {
                isRunning = false;
            }
        });

        socketThread.start();
    }

    public void stopListening() {
        isRunning = false;
        if (socketThread != null) {
            socketThread.interrupt();
            socketThread = null;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
