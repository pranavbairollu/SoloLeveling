package com.example.sololeveling.data.pref

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("solo_leveling_prefs", Context.MODE_PRIVATE)

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
