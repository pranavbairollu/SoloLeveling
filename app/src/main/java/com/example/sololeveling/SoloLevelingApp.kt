package com.example.sololeveling

import android.app.Application
import com.example.sololeveling.data.db.SystemDatabase
import com.example.sololeveling.data.repository.GateRepository
import com.example.sololeveling.data.repository.QuestRepository
import com.example.sololeveling.data.repository.UserRepository
import com.example.sololeveling.data.repository.BossRepository
import com.example.sololeveling.data.repository.ShadowRepository
import com.example.sololeveling.util.SecurityUtils
import android.util.Log

class SoloLevelingApp : Application() {

    val database by lazy { SystemDatabase.getDatabase(this) }
    val userRepository by lazy { UserRepository(database.userDao()) }
    val questRepository by lazy { QuestRepository(database.questDao()) }
    val gateRepository by lazy { GateRepository(database.gateDao()) }
    val bossRepository by lazy { BossRepository(database.bossDao(), database.gateDao()) }
    val shadowRepository by lazy { ShadowRepository(database.shadowDao(), database.questDao()) }

    override fun onCreate() {
        super.onCreate()
        
        // System Integrity Check
        if (SecurityUtils.isDeviceRooted()) {
            Log.e("SoloLevelingSystem", "CRITICAL WARNING: Root Access Detected. System Integrity Compromised.")
        }
        if (SecurityUtils.isRunningOnEmulator()) {
            Log.w("SoloLevelingSystem", "WARNING: Running on Simulation (Emulator). Data may be unstable.")
        }

        com.example.sololeveling.worker.MidnightResetWorker.schedule(this)
    }
}
