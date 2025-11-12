package network.beechat.kaonic.video.sender;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class LocalPipelineUdpManager {
    private static final String TAG = "LocalPipelineManager";
    private static final int PORT = 5005;
    private static final int TS_PACKET_SIZE = 188;

    private final ByteArrayOutputStream packetBuffer = new ByteArrayOutputStream();
    private Thread socketThread;
    private volatile boolean isRunning = false;

    public interface ByteListener {
        void onBytesReceived(byte[] data, int length);
    }

    private final ByteListener listener;

    public LocalPipelineUdpManager(ByteListener listener) {
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
            try (DatagramSocket socket = new DatagramSocket(PORT)) {
                socket.setSoTimeout(0); // No timeout
                Log.i(TAG, "Listening for UDP packets on port " + PORT);

                byte[] receiveBuffer = new byte[188*10]; // Can hold multiple TS packets
                DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                while (isRunning) {
                    socket.receive(packet);
                    int bytesRead = packet.getLength();

                    if (bytesRead > 0) {
                        packetBuffer.write(packet.getData(), 0, bytesRead);

                        byte[] fullData = packetBuffer.toByteArray();
                        int totalLength = fullData.length;
                        Log.i(TAG, "totalLength read " + totalLength);

                        int completePacketsLength = (totalLength / TS_PACKET_SIZE) * TS_PACKET_SIZE;

                        if (completePacketsLength > 0) {
                            byte[] completePackets = new byte[completePacketsLength];
                            System.arraycopy(fullData, 0, completePackets, 0, completePacketsLength);

                            listener.onBytesReceived(completePackets, completePacketsLength);
                            Log.d(TAG, "Sent " + (completePacketsLength / TS_PACKET_SIZE) + " TS packets (" + completePacketsLength + " bytes)");
                        }

                        packetBuffer.reset();
                        if (completePacketsLength < totalLength) {
                            int remainingBytes = totalLength - completePacketsLength;
                            packetBuffer.write(fullData, completePacketsLength, remainingBytes);
                            Log.d(TAG, "Buffered " + remainingBytes + " remaining bytes");
                        }
                    }
                }

                Log.i(TAG, "UDP receiver stopped");

            } catch (IOException e) {
                Log.e(TAG, "UDP socket error: " + e.getMessage(), e);
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