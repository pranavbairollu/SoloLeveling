package com.example.sololeveling.util

import android.content.Context
import android.os.Build
import androidx.security.crypto.MasterKey
import java.io.File
import java.security.SecureRandom

object SecurityUtils {

    /**
     * Generates or retrieves a master key for encryption using Android Keystore.
     */
    fun getOrCreateMasterKey(context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    /**
     * Generates a secure passphrase for the database. 
     * In a production app, this should be stored in the Keystore or 
     * derived from a hardware-backed key.
     */
    fun getDatabasePassphrase(context: Context): ByteArray {
        val masterKey = getOrCreateMasterKey(context)
        // For simplicity in this RPG context, we use a fixed but unique-to-device string.
        // In a real banking app, you'd use a more complex derivation.
        return "SoloLeveling_System_Key_${Build.FINGERPRINT}".toByteArray()
    }

    /**
     * Checks if the device is rooted.
     * This is a basic check; sophisticated root hide tools might bypass it.
     */
    fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    /**
     * Checks if the app is running on an emulator.
     */
    fun isRunningOnEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
    }
}
