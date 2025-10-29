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

                byte[] readBuffer = new byte[188*10];
                int bytesRead;

                while (isRunning && (bytesRead = inputStream.read(readBuffer)) != -1) {
                    if (bytesRead > 0) {
                        packetBuffer.write(readBuffer, 0, bytesRead);

                        byte[] fullData = packetBuffer.toByteArray();
                        int totalLength = fullData.length;
                        Log.i(TAG,"totalLength read "+totalLength);
                        
                        // Check if we have complete TS packets (multiple of 188)
                        int completePacketsLength = (totalLength / 188) * 188;
                        
                        if (completePacketsLength > 0) {
                            // Send all complete packets at once
                            byte[] completePackets = new byte[completePacketsLength];
                            System.arraycopy(fullData, 0, completePackets, 0, completePacketsLength);
                            
                            // --- forward all packets to listener ---
                            listener.onBytesReceived(completePackets, completePacketsLength);
                            
                            Log.d(TAG, "Sent " + (completePacketsLength / 188) + " TS packets (" + completePacketsLength + " bytes)");
                        }

                        // Keep remaining partial bytes
                        packetBuffer.reset();
                        if (completePacketsLength < totalLength) {
                            int remainingBytes = totalLength - completePacketsLength;
                            packetBuffer.write(fullData, completePacketsLength, remainingBytes);
                            Log.d(TAG, "Buffered " + remainingBytes + " remaining bytes");
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
