package com.example.sololeveling.data.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import com.example.sololeveling.util.SecurityUtils

class UserPreferences(context: Context) {
    private val masterKey = SecurityUtils.getOrCreateMasterKey(context)
    
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "solo_leveling_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_USER_CLASS = "user_class"
    }

    fun saveUserClass(className: String) {
        prefs.edit().putString(KEY_USER_CLASS, className).apply()
    }

    fun getUserClass(): String {
        return prefs.getString(KEY_USER_CLASS, "Custom") ?: "Custom"
    }
}
