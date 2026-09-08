package com.module.notelycompose.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSData
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * Stores the OpenRouter API key in the iOS Keychain (kSecClassGenericPassword), never in
 * NSUserDefaults/DataStore.
 *
 * NOTE: this file could not be compile-verified in this session (no Mac/Xcode toolchain
 * available). Kotlin/Native's CFString <-> NSString bridging for the kSec* constants is correct
 * per Apple's documented toll-free bridging, but this is the single highest-risk file in this
 * change — build it first and confirm read/write/clear against a real Keychain before relying on
 * it for anything but local testing.
 */
@OptIn(ExperimentalForeignApi::class)
actual class SecureKeyStore {

    private val service = "com.module.notelycompose.openrouter"
    private val account = "api_key"

    @Suppress("UNCHECKED_CAST")
    private fun baseQuery(): NSMutableDictionary {
        val dict = NSMutableDictionary()
        dict.setObject(kSecClassGenericPassword, forKey = CFBridgingRelease(kSecClass) as NSString)
        dict.setObject(service as NSString, forKey = CFBridgingRelease(kSecAttrService) as NSString)
        dict.setObject(account as NSString, forKey = CFBridgingRelease(kSecAttrAccount) as NSString)
        return dict
    }

    @Suppress("UNCHECKED_CAST")
    actual fun getApiKey(): String? {
        val query = baseQuery()
        query.setObject(kSecMatchLimitOne, forKey = CFBridgingRelease(kSecMatchLimit) as NSString)
        query.setObject(true, forKey = CFBridgingRelease(kSecReturnData) as NSString)

        return memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, result.ptr)
            if (status == errSecSuccess) {
                val data = CFBridgingRelease(result.value) as? NSData
                data?.let { NSString.create(it, NSUTF8StringEncoding) as String? }
            } else {
                null
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    actual fun setApiKey(key: String) {
        // Remove any existing entry first — SecItemAdd fails with errSecDuplicateItem otherwise.
        clearApiKey()

        val data = (key as NSString).dataUsingEncoding(NSUTF8StringEncoding)
        val query = baseQuery()
        query.setObject(data as Any, forKey = CFBridgingRelease(kSecValueData) as NSString)
        SecItemAdd(query as CFDictionaryRef, null)
    }

    actual fun clearApiKey() {
        SecItemDelete(baseQuery() as CFDictionaryRef)
    }
}
