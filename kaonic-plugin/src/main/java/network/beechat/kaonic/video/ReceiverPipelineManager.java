package network.beechat.kaonic.video;

import android.util.Log;
import android.view.Surface;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ReceiverPipelineManager {
    static {
        System.loadLibrary("video-receiver");
    }

    private long nativeHandle = 0;
    
    // Reordering buffers per PID
    private final Map<Integer, TreeMap<Integer, byte[]>> pidBuffers = new HashMap<>();
    private final Map<Integer, Integer> expectedCC = new HashMap<>();
    
    private static final int MAX_BUFFER_SIZE = 32; // Maximum packets to buffer per PID

    public ReceiverPipelineManager(Surface surface) {
        nativeHandle = nativeInit(surface);
    }

    public void feedBytes(byte[] data, int length) {
        if (nativeHandle != 0 && data != null) {
            // Process data in 188-byte TS packet chunks
            int offset = 0;
            while (offset + 188 <= length) {
                byte[] tsPacket = new byte[188];
                System.arraycopy(data, offset, tsPacket, 0, 188);
                
                // Parse TS packet header
                int syncByte = tsPacket[0] & 0xFF;
                if (syncByte != 0x47) {
                    Log.w("TS-Debug", "Invalid sync byte: 0x" + Integer.toHexString(syncByte));
                    offset += 188;
                    continue;
                }
                
                int pid = ((tsPacket[1] & 0x1F) << 8) | (tsPacket[2] & 0xFF);
                int continuityCounter = tsPacket[3] & 0x0F;
                boolean payloadStart = (tsPacket[1] & 0x40) != 0;

//                Log.d("TS-Debug", String.format(
//                        "Received TS Packet: PID=0x%04X, CC=%d, payloadStart=%s",
//                        pid, continuityCounter, payloadStart ? "true" : "false"
//                ));
                
                // Add packet to reordering buffer
                addPacketToBuffer(pid, continuityCounter, tsPacket);
                
                // Try to flush ordered packets
                flushOrderedPackets(pid);
                
                offset += 188;
            }
        }
    }
    
    private void addPacketToBuffer(int pid, int continuityCounter, byte[] tsPacket) {
        // Get or create buffer for this PID
        TreeMap<Integer, byte[]> buffer = pidBuffers.get(pid);
        if (buffer == null) {
            buffer = new TreeMap<>();
            pidBuffers.put(pid, buffer);
        }
        
        // Add packet to buffer (TreeMap automatically sorts by CC)
        buffer.put(continuityCounter, tsPacket.clone());
        
        // Prevent buffer overflow
        if (buffer.size() > MAX_BUFFER_SIZE) {
            // Remove oldest entry
            Integer firstKey = buffer.firstKey();
            buffer.remove(firstKey);
//            Log.w("TS-Debug", "Buffer overflow for PID 0x" + Integer.toHexString(pid) +
//                  ", dropped packet with CC=" + firstKey);
        }
    }
    
    private void flushOrderedPackets(int pid) {
        TreeMap<Integer, byte[]> buffer = pidBuffers.get(pid);
        if (buffer == null || buffer.isEmpty()) {
            return;
        }
        
        Integer expected = expectedCC.get(pid);
        if (expected == null) {
            // First packet for this PID, start with the lowest CC we have
            expected = buffer.firstKey();
            expectedCC.put(pid, expected);
        }
        
        // Process consecutive packets
        while (buffer.containsKey(expected)) {
            byte[] packet = buffer.remove(expected);
            
//            Log.d("TS-Debug", String.format(
//                    "Pushing ordered packet: PID=0x%04X, CC=%d", pid, expected
//            ));
            
            nativePush(nativeHandle, packet, 188);
            
            // Move to next expected CC (wraps around at 16)
            expected = (expected + 1) % 16;
            expectedCC.put(pid, expected);
        }
    }

    public void stop() {
        if (nativeHandle != 0) {
            // Clear all buffers
            pidBuffers.clear();
            expectedCC.clear();
            
            nativeStop(nativeHandle);
            nativeHandle = 0;
        }
    }


    // JNI bindings
    private native long nativeInit(Surface surface);
    private native void nativePush(long handle, byte[] data, int length);
    private native void nativeStop(long handle);
}