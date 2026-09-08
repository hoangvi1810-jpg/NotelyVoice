package com.module.notelycompose.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
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
 * Builds the Keychain query as a plain CFMutableDictionary instead of NSMutableDictionary:
 * every kSec* constant is a raw, unowned CFTypeRef and is passed straight into
 * CFDictionarySetValue (a C function taking `const void *`, no ObjC/ARC bridging involved).
 * The only values that need ownership transfer are the ones we create ourselves
 * (service/account strings, the key's NSData) — those go through CFBridgingRetain, which
 * matches exactly the +1 retain that the dictionary's kCFTypeDictionaryValueCallBacks expects
 * and later balances with its own CFRelease when an entry is removed or the dict itself is
 * released.
 *
 * NOTE: this file could not be compile-verified in this session (no Mac/Xcode toolchain
 * available) — a previous version of this file (mixing NSMutableDictionary with
 * CFBridgingRelease on unowned kSec* constants) crashed on-device when saving a key.
 */
@OptIn(ExperimentalForeignApi::class)
actual class SecureKeyStore {

    private val service = "com.module.notelycompose.openrouter"
    private val account = "api_key"

    private fun newQuery(): CFMutableDictionaryRef {
        val dict = CFDictionaryCreateMutable(
            null,
            0,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr
        )
        CFDictionarySetValue(dict, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(dict, kSecAttrService, CFBridgingRetain(service as NSString))
        CFDictionarySetValue(dict, kSecAttrAccount, CFBridgingRetain(account as NSString))
        return dict!!
    }

    actual fun getApiKey(): String? {
        val query = newQuery()
        CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
        CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)

        return try {
            memScoped {
                val result = alloc<CFTypeRefVar>()
                val status = SecItemCopyMatching(query, result.ptr)
                if (status == errSecSuccess) {
                    val data = CFBridgingRelease(result.value) as? NSData
                    data?.let { NSString.create(it, NSUTF8StringEncoding) as String? }
                } else {
                    null
                }
            }
        } finally {
            CFRelease(query)
        }
    }

    actual fun setApiKey(key: String) {
        clearApiKey()

        val data = (key as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val query = newQuery()
        try {
            CFDictionarySetValue(query, kSecValueData, CFBridgingRetain(data))
            SecItemAdd(query, null)
        } finally {
            CFRelease(query)
        }
    }

    actual fun clearApiKey() {
        val query = newQuery()
        try {
            SecItemDelete(query)
        } finally {
            CFRelease(query)
        }
    }
}
