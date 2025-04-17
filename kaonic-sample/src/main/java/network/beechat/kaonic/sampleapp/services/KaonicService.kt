package network.beechat.kaonic.sampleapp.services

import network.beechat.kaonic.libsource.Kaonic

object KaonicService {
    private lateinit var kaonic: Kaonic

    fun init(kaonic: Kaonic) {
        this.kaonic = kaonic

    }

    fun example() {
        if (!::kaonic.isInitialized) {
            throw IllegalStateException("Kaonic not initialized yet.")
        }
    }
}