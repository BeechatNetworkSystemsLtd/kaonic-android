package network.beechat.kaonic.video.sender;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class LocalPipelineManager {
    private static final String TAG = "LocalPipelineManager";
    ByteArrayOutputStream packetBuffer = new ByteArrayOutputStream();

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
        packetBuffer.reset();
        socketThread = new Thread(() -> {
            try (Socket socket = new Socket(host, 5005);
                 InputStream inputStream = socket.getInputStream()) {

                Log.i(TAG, "Connected to pipeline at " + host + ":" + "5005");

                byte[] readBuffer = new byte[4096];
                int bytesRead;

                while (isRunning && (bytesRead = inputStream.read(readBuffer)) != -1) {
                    if (bytesRead > 0) {
                        packetBuffer.write(readBuffer, 0, bytesRead);

                        byte[] fullData = packetBuffer.toByteArray();
                        int offset = 0;
                        int totalLength = fullData.length;

                        while (totalLength - offset >= 188) {
                            byte[] tsPacket = new byte[188];
                            System.arraycopy(fullData, offset, tsPacket, 0, 188);

                            // --- MPEG-TS header logging ---


                            // --- forward to listener ---
                            listener.onBytesReceived(tsPacket, 188);

                            offset += 188;
                        }

                        // Keep remaining partial bytes
                        packetBuffer.reset();
                        if (offset < fullData.length) {
                            packetBuffer.write(fullData, offset, fullData.length - offset);
                        }
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
