package network.beechat.kaonic.libsource

import android.content.Context

class Kaonic(context: Context) {

    companion object {
        init {
           System.loadLibrary("kaonic")
        }
    }

    fun transmit(address:String, bytes:ByteArray){

    }

    fun received(address:String, bytes:ByteArray){

    }
}
