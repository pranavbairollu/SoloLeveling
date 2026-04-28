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
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            // Emulate boot process / animation time
            delay(2000) 
            
            // Check usage of Repo directly for simplicity in Refactor or use ViewModel
            val database = SystemDatabase.getDatabase(applicationContext)
            val userDao = database.userDao()
            
            val user = withContext(Dispatchers.IO) {
                userDao.getUserSync()
            }
            
            val targetIntent = if (user != null && user.onboardingCompleted) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, SetupActivity::class.java)
            }
            
            startActivity(targetIntent)
            finish()
        }
    }
}
