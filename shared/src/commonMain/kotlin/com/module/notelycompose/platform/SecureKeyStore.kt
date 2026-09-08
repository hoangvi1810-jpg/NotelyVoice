package com.module.notelycompose.platform

/**
 * Stores the OpenRouter API key in OS-level secure storage: Android Keystore-backed AES-GCM
 * encryption on Android, the system Keychain on iOS. Deliberately NOT DataStore/plain
 * SharedPreferences — that would put a live, billable API key in a plaintext file on disk.
 */
expect class SecureKeyStore {
    fun getApiKey(): String?
    fun setApiKey(key: String)
    fun clearApiKey()
}
