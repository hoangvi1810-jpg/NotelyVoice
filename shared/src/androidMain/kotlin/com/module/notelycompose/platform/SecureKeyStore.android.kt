package com.module.notelycompose.platform

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * `androidx.security:security-crypto` (EncryptedSharedPreferences) was deprecated by Google in
 * 2025 with no successor library, so this implements the same idea directly: an AES-256-GCM key
 * held in the Android Keystore (never leaves secure hardware/TEE) encrypts the API key, and only
 * the ciphertext + IV are written to a private SharedPreferences file.
 */
actual class SecureKeyStore(private val context: Context) {

    private val androidKeyStoreProvider = "AndroidKeyStore"
    private val keyAlias = "notely_openrouter_key"
    private val prefsName = "notely_secure_prefs"
    private val prefKeyCiphertext = "api_key_ciphertext"
    private val prefKeyIv = "api_key_iv"

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(androidKeyStoreProvider).apply { load(null) }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        (keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry)?.let {
            return it.secretKey
        }
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            androidKeyStoreProvider
        )
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun prefs() = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    actual fun getApiKey(): String? {
        val prefs = prefs()
        val ciphertextB64 = prefs.getString(prefKeyCiphertext, null) ?: return null
        val ivB64 = prefs.getString(prefKeyIv, null) ?: return null
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = Base64.decode(ivB64, Base64.NO_WRAP)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(128, iv))
            val plaintext = cipher.doFinal(Base64.decode(ciphertextB64, Base64.NO_WRAP))
            String(plaintext, Charsets.UTF_8)
        } catch (e: Exception) {
            // Keystore key invalidated (device lock settings changed, entry corrupted, etc.) —
            // treat as "no key configured" instead of crashing the app.
            null
        }
    }

    actual fun setApiKey(key: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val ciphertext = cipher.doFinal(key.toByteArray(Charsets.UTF_8))
        prefs().edit()
            .putString(prefKeyCiphertext, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
            .putString(prefKeyIv, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    actual fun clearApiKey() {
        prefs().edit().clear().apply()
        runCatching { keyStore.deleteEntry(keyAlias) }
    }
}
