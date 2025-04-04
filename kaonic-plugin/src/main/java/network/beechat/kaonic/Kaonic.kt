package network.beechat.kaonic

import android.content.Context

class Kaonic(context: Context) {

    companion object {
        init {
            System.loadLibrary("kaonic")
            libraryInit()
        }

        @JvmStatic external fun libraryInit()
    }
}
