package network.beechat.kaonic.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class SecureStorageHelper {

    private static final String KEY_ALIAS = "KaonicStorageKey";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String PREFS_NAME = "secure_prefs";
    private static final String AES_MODE = "AES/GCM/NoPadding";

    private SharedPreferences sharedPreferences;

    public SecureStorageHelper(Context context) throws Exception {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        createKeyIfNeeded();
    }

    private void createKeyIfNeeded() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            keyGenerator.init(new KeyGenParameterSpec.Builder(
                            KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
                    )
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build()
            );
            keyGenerator.generateKey();
        }
    }

    private SecretKey getSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
    }

    public void put(String key, String value) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
        byte[] iv = cipher.getIV();
        byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

        String combined = Base64.encodeToString(iv, Base64.DEFAULT) + ":" + Base64.encodeToString(ciphertext, Base64.DEFAULT);
        sharedPreferences.edit().putString(key, combined).apply();
    }

    public String get(String key) throws Exception {
        String combined = sharedPreferences.getString(key, null);
        if (combined == null) {
            return null;
        }

        String[] parts = combined.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid encrypted data");
        }

        byte[] iv = Base64.decode(parts[0], Base64.DEFAULT);
        byte[] ciphertext = Base64.decode(parts[1], Base64.DEFAULT);

        Cipher cipher = Cipher.getInstance(AES_MODE);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec);

        byte[] decrypted = cipher.doFinal(ciphertext);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}