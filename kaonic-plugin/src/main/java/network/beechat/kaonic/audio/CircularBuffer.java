package network.beechat.kaonic.audio;

public class CircularBuffer {
    private final byte[] buffer;
    private int writePos = 0;
    private int readPos = 0;
    private int availableData = 0;

    public CircularBuffer(int size) {
        this.buffer = new byte[size];
    }

    public synchronized void write(byte[] data, int offset, int length) {
        for (int i = 0; i < length; i++) {
            buffer[writePos] = data[offset + i];
            writePos = (writePos + 1) % buffer.length;

            if (availableData < buffer.length) {
                availableData++;
            } else {
                // Overwriting old data
                readPos = (readPos + 1) % buffer.length;
            }
        }
    }

    public synchronized int read(byte[] data, int offset, int length) {
        int bytesRead = 0;
        while (bytesRead < length && availableData > 0) {
            data[offset + bytesRead] = buffer[readPos];
            readPos = (readPos + 1) % buffer.length;
            bytesRead++;
            availableData--;
        }
        return bytesRead;
    }

    public synchronized boolean hasSufficientData(int requiredSize) {
        return availableData >= requiredSize;
    }
}