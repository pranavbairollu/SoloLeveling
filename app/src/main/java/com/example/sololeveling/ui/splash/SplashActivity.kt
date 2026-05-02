package com.example.sololeveling.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sololeveling.MainActivity
import com.example.sololeveling.ui.setup.SetupActivity
import com.example.sololeveling.data.db.SystemDatabase
import com.example.sololeveling.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = com.example.sololeveling.databinding.ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val logs = listOf(
                "> INITIALIZING SYSTEM BOOT...",
                "> SCANNING BIOMETRIC DATA...",
                "> DNA MATCH FOUND: UNKNOWN PLAYER",
                "> CONNECTING TO THE ARCHITECT...",
                "> ACCESS GRANTED",
                "> INITIALIZING MONARCH PROTOCOL...",
                "> SYSTEM ONLINE"
            )

            var currentLog = ""
            for (i in logs.indices) {
                currentLog += logs[i] + "\n"
                binding.tvBootLogs.text = currentLog
                binding.pbBoot.progress = ((i + 1) * 100) / logs.size
                delay(400) // Delay for each log line
            }
            
            delay(500) // Final pause
            
            val database = SystemDatabase.getDatabase(applicationContext)
            val user = withContext(Dispatchers.IO) {
                database.userDao().getUserSync()
            }
            
            val targetIntent = if (user != null && user.onboardingCompleted) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, SetupActivity::class.java)
            }
            
            startActivity(targetIntent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}
