package com.example.sololeveling.ui.gate

import android.content.*
import android.os.*
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sololeveling.R
import com.example.sololeveling.databinding.ActivityGateBinding
import com.example.sololeveling.data.db.SystemDatabase
import com.example.sololeveling.data.entity.GateEntity
import com.example.sololeveling.service.GateFocusService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class GateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGateBinding
    private val database by lazy { SystemDatabase.getDatabase(this) }
    private val gateDao by lazy { database.gateDao() }

    private var gateId: Int = -1
    private var isRedGate: Boolean = false
    private var gate: GateEntity? = null
    
    private var focusService: GateFocusService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as GateFocusService.LocalBinder
            focusService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            focusService = null
        }
    }

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                GateFocusService.ACTION_TIMER_TICK -> {
                    val remaining = intent.getLongExtra("REMAINING", 0L)
                    updateTimerUI(remaining)
                }
                GateFocusService.ACTION_GATE_SUCCESS -> handleSuccess()
                GateFocusService.ACTION_GATE_FAILURE -> {
                    val reason = intent.getStringExtra("REASON") ?: "Unknown"
                    handleFailure(reason)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityGateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gateId = intent.getIntExtra("GATE_ID", -1)
        
        if (gateId == -1) {
            finish()
            return
        }

        // 15% Chance of Red Gate upgrade if not already determined
        isRedGate = intent.getBooleanExtra("IS_RED_GATE", Random.nextFloat() < 0.15f)

        setupUI()
        loadGateData()
        
        val filter = IntentFilter().apply {
            addAction(GateFocusService.ACTION_TIMER_TICK)
            addAction(GateFocusService.ACTION_GATE_SUCCESS)
            addAction(GateFocusService.ACTION_GATE_FAILURE)
        }
        registerReceiver(timerReceiver, filter, RECEIVER_EXPORTED)
    }

    private fun setupUI() {
        if (isRedGate) {
            applyRedGateAesthetics()
        }
        
        binding.btnSurrender.setOnClickListener {
            showSurrenderConfirmation()
        }
    }

    private fun applyRedGateAesthetics() {
        binding.rootLayout.setBackgroundColor(0xFF220000.toInt())
        binding.tvRedGateWarning.visibility = View.VISIBLE
        com.example.sololeveling.ui.common.AnimUtils.typewriter(binding.tvRedGateWarning, "SYSTEM ERROR: RED GATE DETECTED\nSTAKES DOUBLED", delayMs = 40)
        binding.tvGateName.setTextColor(0xFFFF0000.toInt())
        com.example.sololeveling.ui.common.AnimUtils.pulse(binding.tvRedGateWarning, 1.05f, 1000)
    }

    private fun loadGateData() {
        lifecycleScope.launch {
            gate = withContext(Dispatchers.IO) { gateDao.getGateById(gateId) }
            gate?.let {
                binding.tvGateName.text = it.name
                if (it.isActive) {
                    resumeGate(it)
                } else {
                    showEntryConfirmation(it)
                }
            } ?: finish()
        }
    }

    private fun showEntryConfirmation(gate: GateEntity) {
        val duration = formatDuration(gate.durationMillis)
        AlertDialog.Builder(this, R.style.Theme_SoloLeveling_Dialog)
            .setTitle("GATE ENTRY DETECTED")
            .setMessage("Dungeon: ${gate.name}\nDuration: $duration\n\nWARNING: Leaving this app will cause immediate failure and a 24-hour cooldown.\n\nRETREAT IS IMPOSSIBLE.")
            .setPositiveButton("ENTER") { _, _ -> startFocusSession(gate) }
            .setNegativeButton("RETREAT") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun startFocusSession(gate: GateEntity) {
        val intent = Intent(this, GateFocusService::class.java).apply {
            putExtra("GATE_ID", gate.id)
            putExtra("DURATION", gate.durationMillis)
            putExtra("IS_RED_GATE", isRedGate)
        }
        startForegroundService(intent)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        
        lifecycleScope.launch(Dispatchers.IO) {
            gateDao.updateGate(gate.copy(
                isActive = true,
                startTimestamp = System.currentTimeMillis(),
                endTimestamp = System.currentTimeMillis() + gate.durationMillis
            ))
        }
    }

    private fun resumeGate(gate: GateEntity) {
        val remaining = gate.endTimestamp - System.currentTimeMillis()
        if (remaining <= 0) {
            handleSuccess()
        } else {
            val intent = Intent(this, GateFocusService::class.java)
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    private fun updateTimerUI(remaining: Long) {
        val hms = String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(remaining),
            TimeUnit.MILLISECONDS.toMinutes(remaining) % 60,
            TimeUnit.MILLISECONDS.toSeconds(remaining) % 60)
        binding.tvTimer.text = hms
        
        gate?.let {
            val progress = ((remaining.toFloat() / it.durationMillis.toFloat()) * 100).toInt()
            binding.progressTimer.progress = progress
        }
    }

    private fun handleSuccess() {
        binding.tvStatus.text = "GATE CLEARED"
        binding.tvStatus.setTextColor(0xFF00FF00.toInt())
        Toast.makeText(this, "Rewards Granted. XP +${gate?.xpReward?.let { if (isRedGate) it * 2 else it }}", Toast.LENGTH_LONG).show()
        lifecycleScope.launch {
            kotlinx.coroutines.delay(2000)
            finish()
        }
    }

    private fun handleFailure(reason: String) {
        binding.tvStatus.text = "GATE COLLAPSED: $reason"
        binding.tvStatus.setTextColor(0xFFFF0000.toInt())
        Toast.makeText(this, "FAILURE. 24H COOLDOWN APPLIED.", Toast.LENGTH_LONG).show()
        lifecycleScope.launch {
            kotlinx.coroutines.delay(2000)
            finish()
        }
    }

    private fun showSurrenderConfirmation() {
        AlertDialog.Builder(this, R.style.Theme_SoloLeveling_Dialog)
            .setTitle("SURRENDER?")
            .setMessage("Giving up now will trigger the penalty.")
            .setPositiveButton("GIVE UP") { _, _ -> focusService?.failGate("SURRENDERED") }
            .setNegativeButton("KEEP FIGHTING", null)
            .show()
    }

    override fun onStop() {
        super.onStop()
        if (!isFinishing && isBound) {
            // App backgrounded!
            focusService?.failGate("FOCUS LOST")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        unregisterReceiver(timerReceiver)
    }

    override fun onBackPressed() {
        com.example.sololeveling.ui.common.AnimUtils.shake(binding.root, intensity = 15f)
        com.example.sololeveling.ui.common.SystemNotifier.show(this, "SYSTEM DOES NOT PERMIT RETREAT", com.example.sololeveling.ui.common.SystemNotificationView.Type.WARNING)
    }

    private fun formatDuration(ms: Long): String {
        return "${TimeUnit.MILLISECONDS.toMinutes(ms)} Minutes"
    }
}
