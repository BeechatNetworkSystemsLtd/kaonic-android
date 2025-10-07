package network.beechat.kaonic.video;

import java.io.ByteArrayOutputStream;

public class ByteArrayBuffer {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public void append(byte[] data, int offset, int length) {
        buffer.write(data, offset, length);
    }

    public byte[] toByteArray() {
        return buffer.toByteArray();
    }

    public int size() {
        return buffer.size();
    }

    public void clear() {
        buffer.reset();
    }
}