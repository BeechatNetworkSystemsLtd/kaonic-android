package network.beechat.kaonic.sampleapp

import android.app.Application
import network.beechat.kaonic.libsource.KaonicLib
import network.beechat.kaonic.sampleapp.services.KaonicService

class SampleApp: Application() {
    override fun onCreate() {
        super.onCreate()

        /// Kaonic plugin init
        KaonicService.init(KaonicLib.getInstance(applicationContext))
    }
}