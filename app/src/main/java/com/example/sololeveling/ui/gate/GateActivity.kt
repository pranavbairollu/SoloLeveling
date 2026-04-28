package com.example.sololeveling.ui.gate

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sololeveling.R
import com.example.sololeveling.databinding.ActivityGateBinding
import com.example.sololeveling.data.db.SystemDatabase
import com.example.sololeveling.data.entity.GateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class GateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGateBinding
    private val database by lazy { SystemDatabase.getDatabase(this) }
    private val gateDao by lazy { database.gateDao() }

    private var gateId: Int = -1
    private var isRedGate: Boolean = false
    private var gate: GateEntity? = null
    
    private var timer: CountDownTimer? = null
    private var isSuccess = false
    private var isFailed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gateId = intent.getIntExtra("GATE_ID", -1)
        isRedGate = intent.getBooleanExtra("IS_RED_GATE", false)

        if (gateId == -1) {
            finish()
            return
        }

        setupUI()
        startGate()
    }

    private fun setupUI() {
        if (isRedGate) {
            binding.rootLayout.setBackgroundColor(0xFF330000.toInt()) // Dark Red
            binding.tvRedGateWarning.visibility = View.VISIBLE
        }
        
        binding.btnSurrender.setOnClickListener {
            failGate(getString(R.string.gate_reason_surrender))
        }
    }

    private fun startGate() {
        lifecycleScope.launch {
            gate = withContext(Dispatchers.IO) {
                gateDao.getGateById(gateId)
            }

            if (gate == null) {
                finish()
                return@launch
            }

            val currentGate = gate!!
            binding.tvGateName.text = currentGate.name
            
            val now = System.currentTimeMillis()

            // 1. Check if already active
            if (currentGate.isActive) {
                // Resume immediately
                resumeGate(currentGate, now)
            } else {
                // 2. Show Confirmation Dialog BEFORE starting
                showEntryConfirmation(currentGate)
            }
        }
    }

    private fun showEntryConfirmation(gate: GateEntity) {
        val duration = if (gate.durationMillis > 0) 
            "${TimeUnit.MILLISECONDS.toMinutes(gate.durationMillis)} Minutes" 
        else "${gate.durationDays} Days"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("WARNING: GATE ENTRY")
            .setMessage("Entering ${gate.name} will lock your device for [$duration].\n\nRETREAT IS IMPOSSIBLE.\n\nLeaving the app will cause IMMEDIATE FAILURE.\n\nDo you wish to proceed?")
            .setPositiveButton("ENTER GATE") { _, _ ->
                initializeGateStart(gate)
            }
            .setNegativeButton("RETREAT") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun initializeGateStart(gate: GateEntity) {
        lifecycleScope.launch {
             val now = System.currentTimeMillis()
             val durationMillis = if (gate.durationMillis > 0) gate.durationMillis else gate.durationDays * 24 * 60 * 60 * 1000L
             val endTime = now + durationMillis

             val activeGate = gate.copy(
                isActive = true,
                startTimestamp = now,
                endTimestamp = endTime
             )
             withContext(Dispatchers.IO) {
                 gateDao.updateGate(activeGate)
             }
             this@GateActivity.gate = activeGate
             startTimer(durationMillis, durationMillis)
        }
    }

    private fun resumeGate(gate: GateEntity, now: Long) {
         val endTime = gate.endTimestamp
         val durationMillis = if (gate.durationMillis > 0) gate.durationMillis else gate.durationDays * 24 * 60 * 60 * 1000L
         val remaining = endTime - now
         
         if (remaining <= 0) {
             finishGateSuccess()
         } else {
             startTimer(remaining, durationMillis)
         }
    }

    private fun startTimer(millisInFuture: Long, totalDuration: Long) {
        timer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (isFinishing || isDestroyed) {
                    cancel()
                    return
                }
                val hms = String.format("%02d:%02d:%02d", 
                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60)
                binding.tvTimer.text = hms
                
                val progress = (millisUntilFinished.toFloat() / totalDuration.toFloat() * 100).toInt()
                binding.progressTimer.progress = progress
            }

            override fun onFinish() {
                binding.tvTimer.text = "00:00:00"
                binding.progressTimer.progress = 0
                finishGateSuccess()
            }
        }.start()
    }

    private fun finishGateSuccess() {
        if (isFailed) return
        isSuccess = true
        binding.tvStatus.text = getString(R.string.gate_cleared)
        binding.tvStatus.setTextColor(resources.getColor(android.R.color.holo_green_light, null))
        binding.btnSurrender.visibility = View.GONE
        
        Toast.makeText(this, getString(R.string.gate_rewards_granted), Toast.LENGTH_LONG).show()
        
        lifecycleScope.launch {
            gate?.let {
                val rewardMult = if (isRedGate) 2 else 1
                val completedGate = it.copy(
                    isActive = false,
                    isCleared = true,
                    xpReward = it.xpReward * rewardMult
                )
                withContext(Dispatchers.IO) {
                    gateDao.updateGate(completedGate)
                }
            }
            kotlinx.coroutines.delay(2000)
            finish()
        }
    }

    private fun failGate(reason: String) {
        if (isSuccess) return
        isFailed = true
        timer?.cancel()
        
        binding.tvStatus.text = getString(R.string.gate_failed_prefix, reason)
        binding.tvStatus.setTextColor(resources.getColor(android.R.color.holo_red_light, null))
        
        lifecycleScope.launch {
             gate?.let {
                val failedGate = it.copy(
                    isActive = false,
                    isFailed = true,
                    cooldownEnd = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24h Penalty
                )
                withContext(Dispatchers.IO) {
                    gateDao.updateGate(failedGate)
                }
            }
            kotlinx.coroutines.delay(2000)
            finish()
        }
    }

    override fun onBackPressed() {
        Toast.makeText(this, getString(R.string.msg_retreat_impossible), Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        gate?.let {
            if (it.isActive && it.endTimestamp > 0) {
                 val now = System.currentTimeMillis()
                 if (now > it.endTimestamp + 5000) { 
                     if (!isSuccess && !isFailed) {
                         finishGateSuccess()
                     }
                 }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isSuccess && !isFinishing) {
             failGate(getString(R.string.gate_reason_focus_lost))
        } else if (isFinishing && !isSuccess && !isFailed) {
             failGate(getString(R.string.gate_reason_collapsed))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
