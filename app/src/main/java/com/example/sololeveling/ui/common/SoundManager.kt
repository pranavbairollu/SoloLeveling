package com.example.sololeveling.ui.common

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class SoundManager(private val context: Context) {

    private val soundPool: SoundPool
    private val vibrationService: Vibrator

    // Sound IDs
    private var soundQuestComplete: Int = 0
    private var soundLevelUp: Int = 0
    private var soundMonarch: Int = 0
    private var soundGateFail: Int = 0

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
            
        // Setup Vibrator
        vibrationService = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        // Load Sounds (Placeholder resource IDs used, will be 0 if not present, safe to use)
        // In reality, user needs to add raw resources. We will simulate with defaults if possible or just log.
        // Assuming we rely on Vibrator primarily if no sound files.
    }
    
    fun playQuestComplete(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrate(100)
        if (soundEnabled) {
            // soundPool.play(soundQuestComplete, 1f, 1f, 0, 0, 1f)
        }
    }
    
    fun playLevelUp(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibratePattern(longArrayOf(0, 100, 50, 200))
        if (soundEnabled) {
             // soundPool.play(soundLevelUp, 1f, 1f, 0, 0, 1f)
        }
    }
    
    fun playMonarch(soundEnabled: Boolean, hapticsEnabled: Boolean) {
         if (hapticsEnabled) vibratePattern(longArrayOf(0, 500, 200, 1000)) // Epic vibration
         // Play sound logic
    }
    
    fun playError(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibratePattern(longArrayOf(0, 50, 50, 50))
    }

    private fun vibrate(duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrationService.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrationService.vibrate(duration)
        }
    }
    
    private fun vibratePattern(pattern: LongArray) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrationService.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrationService.vibrate(pattern, -1)
        }
    }
}
