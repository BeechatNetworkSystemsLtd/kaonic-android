package network.beechat.kaonic.sampleapp.services

import network.beechat.kaonic.libsource.KaonicLib

object KaonicService {
    private lateinit var kaonic: KaonicLib

    fun init(kaonic: KaonicLib){
        this.kaonic = kaonic

    }

    fun example() {
        if (!::kaonic.isInitialized) {
            throw IllegalStateException("Kaonic not initialized yet.")
        }
    }
}