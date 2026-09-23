package com.example.n074_vraj_assignment1

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureKeyStorage(context: Context) {

    private var sharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyGenParameterSpec(
                KeyGenParameterSpec.Builder(
                    MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    } catch (e: Exception) {
        Log.e("SecureKeyStorage", "Failed to initialize EncryptedSharedPreferences", e)
        null
    }

    fun getDecryptedApiKey(): String {
        val buildKey = BuildConfig.GEMINI_API_KEY
        if (buildKey.isNotBlank()) {
            try {
                sharedPreferences?.edit {
                    putString("GEMINI_KEY", buildKey)
                }
            } catch (e: Exception) {
                Log.e("SecureKeyStorage", "Failed to write API key to EncryptedSharedPreferences", e)
            }
            return buildKey
        }
        return try {
            sharedPreferences?.getString("GEMINI_KEY", "") ?: ""
        } catch (e: Exception) {
            Log.e("SecureKeyStorage", "Failed to read API key from EncryptedSharedPreferences", e)
            ""
        }
    }
}
