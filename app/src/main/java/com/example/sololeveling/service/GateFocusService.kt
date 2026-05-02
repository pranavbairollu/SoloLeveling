package com.example.sololeveling.service

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.sololeveling.MainActivity
import com.example.sololeveling.R
import com.example.sololeveling.data.db.SystemDatabase
import com.example.sololeveling.data.entity.GateEntity
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class GateFocusService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var timer: CountDownTimer? = null
    private var activeGateId: Int = -1
    private var isRedGate: Boolean = false
    private var endTime: Long = 0L
    
    private var isSuccess = false
    private var isFailed = false

    inner class LocalBinder : Binder() {
        fun getService(): GateFocusService = this@GateFocusService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val gateId = intent?.getIntExtra("GATE_ID", -1) ?: -1
        val durationMillis = intent?.getLongExtra("DURATION", 0L) ?: 0L
        val isRed = intent?.getBooleanExtra("IS_RED_GATE", false) ?: false

        if (gateId != -1 && durationMillis > 0) {
            activeGateId = gateId
            isRedGate = isRed
            startForeground(NOTIFICATION_ID, createNotification("Gate Active", "Focus or die."))
            startTimer(durationMillis)
        }

        return START_NOT_STICKY
    }

    private fun startTimer(duration: Long) {
        endTime = SystemClock.elapsedRealtime() + duration
        
        timer?.cancel()
        timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hms = formatTime(millisUntilFinished)
                updateNotification("Gate Active: $hms", if (isRedGate) "RED GATE: HIGH STAKES" else "Stay focused.")
                
                // Broadcast update to Activity
                val intent = Intent(ACTION_TIMER_TICK).apply {
                    putExtra("REMAINING", millisUntilFinished)
                }
                sendBroadcast(intent)
            }

            override fun onFinish() {
                handleSuccess()
            }
        }.start()
    }

    private fun handleSuccess() {
        if (isFailed || isSuccess) return
        isSuccess = true
        
        serviceScope.launch {
            val db = SystemDatabase.getDatabase(applicationContext)
            val gateDao = db.gateDao()
            val userRepo = com.example.sololeveling.data.repository.UserRepository(db.userDao())
            val gate = gateDao.getGateById(activeGateId)
            
            gate?.let {
                val rewardMult = if (isRedGate) 2 else 1
                val baseXP = it.xpReward
                val totalXP = baseXP * rewardMult
                
                // Update Gate
                val completedGate = it.copy(
                    isActive = false,
                    isCleared = true
                )
                gateDao.updateGate(completedGate)
                
                // Grant Rewards to User
                userRepo.grantGateRewards(totalXP)
            }
            
            val intent = Intent(ACTION_GATE_SUCCESS)
            sendBroadcast(intent)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    fun failGate(reason: String) {
        if (isSuccess || isFailed) return
        isFailed = true
        timer?.cancel()

        serviceScope.launch {
            val db = SystemDatabase.getDatabase(applicationContext)
            val gateDao = db.gateDao()
            val userRepo = com.example.sololeveling.data.repository.UserRepository(db.userDao())
            val gate = gateDao.getGateById(activeGateId)
            
            gate?.let {
                val penaltyMult = if (isRedGate) 2 else 1
                val cooldown = (24 * 60 * 60 * 1000L) * penaltyMult
                
                val failedGate = it.copy(
                    isActive = false,
                    isFailed = true,
                    cooldownEnd = System.currentTimeMillis() + cooldown
                )
                gateDao.updateGate(failedGate)

                // High Stakes: Endurance Loss
                val user = userRepo.getCurrentUser()
                if (user != null) {
                    val enduranceLoss = if (isRedGate) 30 else 10
                    userRepo.decreaseEndurance(user, enduranceLoss)
                }
            }
            
            val intent = Intent(ACTION_GATE_FAILURE).apply {
                putExtra("REASON", reason)
            }
            sendBroadcast(intent)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotification(title: String, text: String): Notification {
        val channelId = "gate_focus_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Gate Focus", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use appropriate icon
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setColor(if (isRedGate) 0xFFFF0000.toInt() else 0xFF0000FF.toInt())
            .build()
    }

    private fun updateNotification(title: String, text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification(title, text))
    }

    private fun formatTime(millis: Long): String {
        return String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
            TimeUnit.MILLISECONDS.toSeconds(millis) % 60)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isSuccess && !isFailed) {
            // Unexpected service kill - trigger failure logic in DB
            failGate("System Collapse")
        }
        timer?.cancel()
        serviceScope.cancel()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_TIMER_TICK = "com.example.sololeveling.TIMER_TICK"
        const val ACTION_GATE_SUCCESS = "com.example.sololeveling.GATE_SUCCESS"
        const val ACTION_GATE_FAILURE = "com.example.sololeveling.GATE_FAILURE"
    }
}
