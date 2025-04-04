package network.beechat.kaonic.audio

class CircularBuffer(size: Int) {
    private val buffer = ByteArray(size)
    private var writePos = 0
    private var readPos = 0
    private var availableData = 0

    @Synchronized
    fun write(data: ByteArray, offset: Int, length: Int) {
        for (i in 0 until length) {
            buffer[writePos] = data[offset + i]
            writePos = (writePos + 1) % buffer.size

            if (availableData < buffer.size) {
                availableData++
            } else {
                // Overwriting old data
                readPos = (readPos + 1) % buffer.size
            }
        }
    }

    @Synchronized
    fun read(data: ByteArray, offset: Int, length: Int): Int {
        var bytesRead = 0
        while (bytesRead < length && availableData > 0) {
            data[offset + bytesRead] = buffer[readPos]
            readPos = (readPos + 1) % buffer.size
            bytesRead++
            availableData--
        }
        return bytesRead
    }

    @Synchronized
    fun hasSufficientData(requiredSize: Int): Boolean {
        return availableData >= requiredSize
    }
}