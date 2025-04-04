package network.beechat.kaonic.audio

interface AudioStreamCallback {
    fun onResult(count:Int,  audioBuffer:ByteArray)
}