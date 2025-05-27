package network.beechat.kaonic.sampleapp.services

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import androidx.core.content.edit

class SecureStorageHelper(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        createKeyIfNeeded()
    }

    @Throws(Exception::class)
    private fun createKeyIfNeeded() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            keyGenerator.generateKey()
        }
    }

    @get:Throws(Exception::class)
    private val secretKey: SecretKey
        get() {
            val keyStore =
                KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            return keyStore.getKey(KEY_ALIAS, null) as SecretKey
        }

    @Throws(Exception::class)
    fun putSecured(key: String?, value: String) {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))

        val combined = Base64.encodeToString(iv, Base64.DEFAULT) + ":" + Base64.encodeToString(
            ciphertext,
            Base64.DEFAULT
        )
        sharedPreferences.edit() { putString(key, combined) }
    }

    @Throws(Exception::class)
    fun getSecured(key: String?): String? {
        val combined = sharedPreferences.getString(key, null) ?: return null

        val parts = combined.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        require(parts.size == 2) { "Invalid encrypted data" }

        val iv = Base64.decode(parts[0], Base64.DEFAULT)
        val ciphertext = Base64.decode(parts[1], Base64.DEFAULT)

        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val decrypted = cipher.doFinal(ciphertext)
        return String(decrypted, StandardCharsets.UTF_8)
    }

    @Throws(Exception::class)
    fun put(key: String?, value: String?) {
        sharedPreferences.edit() { putString(key, value) }
    }

    @Throws(Exception::class)
    fun get(key: String?): String? {
        return sharedPreferences.getString(key, null)
    }

    companion object {
        private const val KEY_ALIAS = "KaonicStorageKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val PREFS_NAME = "secure_prefs"
        private const val AES_MODE = "AES/GCM/NoPadding"
    }
}