
package com.example.sololeveling

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.addCallback
import com.example.sololeveling.databinding.ActivityMainBinding
import com.example.sololeveling.ui.common.SystemViewModelFactory
import com.example.sololeveling.ui.dashboard.MainViewModel
import com.example.sololeveling.ui.dashboard.QuestAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private val adapter = QuestAdapter { quest ->
        viewModel.completeQuest(quest)
        com.example.sololeveling.ui.common.SystemNotifier.show(this, "QUEST COMPLETED", com.example.sololeveling.ui.common.SystemNotificationView.Type.INFO)
    }

    private lateinit var soundManager: com.example.sololeveling.ui.common.SoundManager

    // Escalation State
    private var recurringHapticHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var recurringHapticRunnable = object : Runnable {
        override fun run() {
            triggerVibration(long = true)
            recurringHapticHandler.postDelayed(this, 15000) // Every 15s in Phase 3
        }
    }
    
    private var pulseState = 0 // 0: None, 1: Weak, 2: Strong
    
    private var lastLevel = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        soundManager = com.example.sololeveling.ui.common.SoundManager(this)

        val app = application as SoloLevelingApp
        val factory = SystemViewModelFactory(app.userRepository, app.questRepository, app.gateRepository, app.bossRepository, app.shadowRepository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun handleEscalationPhase(phase: com.example.sololeveling.util.QuestSystemManager.EscalationPhase) {
        // Reset haptics
        recurringHapticHandler.removeCallbacks(recurringHapticRunnable)
        
        when (phase) {
            com.example.sololeveling.util.QuestSystemManager.EscalationPhase.NONE -> {
                stopTimerPulse()
                stopPenaltyPulse()
            }
            com.example.sololeveling.util.QuestSystemManager.EscalationPhase.PHASE_1 -> {
                ensureTimerPulse(strong = false)
                stopPenaltyPulse()
            }
            com.example.sololeveling.util.QuestSystemManager.EscalationPhase.PHASE_2 -> {
                ensureTimerPulse(strong = true)
                stopPenaltyPulse()
            }
            com.example.sololeveling.util.QuestSystemManager.EscalationPhase.PHASE_3 -> {
                ensureTimerPulse(strong = true)
                ensurePenaltyPulse()
                recurringHapticRunnable.run() // Start recurring vibrations
            }
            com.example.sololeveling.util.QuestSystemManager.EscalationPhase.PENALTY -> {
                stopTimerPulse()
                ensurePenaltyPulse()
            }
        }
    }

    private fun ensureTimerPulse(strong: Boolean) {
        val targetState = if (strong) 2 else 1
        if (pulseState == targetState) return
        
        pulseState = targetState
        binding.tvDailyTimer.clearAnimation() 
        
        val scale = if (strong) 1.1f else 1.05f
        val duration = if (strong) 500L else 1000L
        
        com.example.sololeveling.ui.common.AnimUtils.pulse(binding.tvDailyTimer, scale, duration)
    }
    
    private fun stopTimerPulse() {
        if (pulseState == 0) return
        pulseState = 0
        binding.tvDailyTimer.clearAnimation()
        binding.tvDailyTimer.scaleX = 1f
        binding.tvDailyTimer.scaleY = 1f
    }
    
    private var isPenaltyPulsing = false
    
    private fun ensurePenaltyPulse() {
        if (isPenaltyPulsing) return
        isPenaltyPulsing = true
        binding.vPenaltyBorder.visibility = android.view.View.VISIBLE
        com.example.sololeveling.ui.common.AnimUtils.glowPulse(binding.vPenaltyBorder)
    }
    
    private fun stopPenaltyPulse() {
        if (!isPenaltyPulsing) return
        isPenaltyPulsing = false
        val animator = binding.vPenaltyBorder.tag as? android.animation.ObjectAnimator
        animator?.cancel()
        binding.vPenaltyBorder.alpha = 0f
        binding.vPenaltyBorder.visibility = android.view.View.GONE
    }

    private fun setupClickListeners() {
        var lastClickTime = 0L
        val debounceTime = 1000L
        
        binding.statsContainer.setOnClickListener {
             if (System.currentTimeMillis() - lastClickTime < debounceTime) return@setOnClickListener
             lastClickTime = System.currentTimeMillis()
             com.example.sololeveling.ui.common.AnimUtils.pulse(it, 1.02f, 200, 0)
             showStatsDialog()
        }
        
        binding.btnGates.setOnClickListener {
            if (System.currentTimeMillis() - lastClickTime < debounceTime) return@setOnClickListener
            lastClickTime = System.currentTimeMillis()
            com.example.sololeveling.ui.common.AnimUtils.pulse(it, 0.95f, 100, 0)
            startActivity(android.content.Intent(this, com.example.sololeveling.ui.gate.GateSelectionActivity::class.java))
        }

        binding.btnBoss.setOnClickListener {
            if (System.currentTimeMillis() - lastClickTime < debounceTime) return@setOnClickListener
            lastClickTime = System.currentTimeMillis()
            com.example.sololeveling.ui.common.AnimUtils.pulse(it, 0.95f, 100, 0)
            startActivity(android.content.Intent(this, com.example.sololeveling.ui.boss.BossActivity::class.java))
        }

        binding.btnShadows.setOnClickListener {
            if (System.currentTimeMillis() - lastClickTime < debounceTime) return@setOnClickListener
            lastClickTime = System.currentTimeMillis()
            com.example.sololeveling.ui.common.AnimUtils.pulse(it, 0.95f, 100, 0)
            startActivity(android.content.Intent(this, com.example.sololeveling.ui.shadow.ShadowListActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        binding.rvQuests.layoutManager = LinearLayoutManager(this)
        binding.rvQuests.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.user.observe(this) { user ->
            user?.let {
                val requiredXp = com.example.sololeveling.util.StatCalculator.calculateRequiredXp(it.level, it.knowledge)
                binding.progressBarXP.max = requiredXp.toInt()
                
                // Level Up Detection
                if (lastLevel == -1) {
                    lastLevel = it.level
                } else if (it.level > lastLevel) {
                    lastLevel = it.level
                    playLevelUpCeremony()
                }
                
                // Animate XP Bar (Normal)
                android.animation.ObjectAnimator.ofInt(binding.progressBarXP, "progress", it.currentXP.toInt())
                    .setDuration(1000)
                    .start()

                binding.tvXpText.text = "${it.currentXP} / $requiredXp"
                
                if (it.isMonarch) {
                    if (binding.tvPlayerName.text != "I AM THE MONARCH") {
                         binding.tvPlayerName.setTextColor(android.graphics.Color.parseColor("#FFD700")) // Gold
                         com.example.sololeveling.ui.common.AnimUtils.typewriter(binding.tvPlayerName, "I AM THE MONARCH")
                    }
                } else if (binding.tvPlayerName.text != "PRANAV") {
                     // Initial Typewriter or Reset
                     binding.tvPlayerName.setTextColor(android.graphics.Color.WHITE)
                     com.example.sololeveling.ui.common.AnimUtils.typewriter(binding.tvPlayerName, "PRANAV")
                }
                
                // Endurance UI & Stats
                binding.tvEnd.text = "HP: ${it.endurance} / ${it.maxEndurance}"
                
                // Stat Grid Binding
                val reduction = it.penaltyStatReduction
                binding.tvStr.text = "STR: ${it.fitness - reduction}"
                binding.tvInt.text = "INT: ${it.knowledge - reduction}"
                binding.tvAwa.text = "AGI: ${it.awareness - reduction}"
                binding.tvDisc.text = "VIT: ${it.discipline - reduction}"
                binding.tvChr.text = "CHR: ${it.charisma - reduction}"
                binding.tvLuk.text = "LUK: ${it.luck - reduction}"
                
                val statColor = if (reduction > 0) getColor(R.color.system_red) else android.graphics.Color.WHITE
                binding.tvStr.setTextColor(statColor)
                binding.tvInt.setTextColor(statColor)
                binding.tvAwa.setTextColor(statColor)
                binding.tvDisc.setTextColor(statColor)
                binding.tvChr.setTextColor(statColor)
                binding.tvLuk.setTextColor(statColor)
                
                if (it.endurance <= 1) {
                    binding.tvEnd.setTextColor(getColor(R.color.system_red))
                    com.example.sololeveling.ui.common.AnimUtils.pulse(binding.tvEnd, 1.2f, 500)
                } else {
                    binding.tvEnd.setTextColor(getColor(R.color.system_neon_green))
                    binding.tvEnd.animation?.cancel()
                    binding.tvEnd.scaleX = 1f
                    binding.tvEnd.scaleY = 1f
                }
                
                // Penalty UI State
                if (System.currentTimeMillis() < it.penaltyEndTime) {
                    binding.tvPlayerName.setTextColor(getColor(R.color.system_red))
                    binding.tvXpText.text = "PENALTY ACTIVE (LOCKED)"
                    binding.tvXpText.setTextColor(getColor(R.color.system_red))
                    binding.progressBarXP.progressTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.system_red))
                } else {
                    binding.tvPlayerName.setTextColor(if (it.isMonarch) android.graphics.Color.MAGENTA else getColor(R.color.system_blue)) 
                    binding.tvXpText.setTextColor(getColor(R.color.system_text_primary))
                    binding.progressBarXP.progressTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.system_blue))
                }
                
                // Rank Display
                binding.tvRank.text = "Rank: ${it.rank}"
                if (it.isMonarch) {
                    binding.tvRank.setTextColor(android.graphics.Color.YELLOW) // Gold
                    binding.tvPlayerName.setTextColor(android.graphics.Color.MAGENTA) // Monarch Aura
                    // Pulse Rank
                    com.example.sololeveling.ui.common.AnimUtils.pulse(binding.tvRank, 1.1f, 1500)
                } else {
                     binding.tvRank.setTextColor(android.graphics.Color.WHITE)
                }
            }
        }

        viewModel.dailyQuests.observe(this) { quests ->
            adapter.submitList(quests)
        }

        viewModel.timeRemaining.observe(this) { diff ->
            val hours = diff / (1000 * 60 * 60)
            val minutes = (diff / (1000 * 60)) % 60
            val seconds = (diff / 1000) % 60
            binding.tvDailyTimer.text = String.format("[ TIME REMAINING: %02d:%02d:%02d ]", hours, minutes, seconds)
        }

        viewModel.escalationPhase.observe(this) { phase ->
            handleEscalationPhase(phase)
        }
        
        viewModel.penaltyEvent.observe(this) { triggered ->
             if (triggered) {
                 showPenaltyDialog()
                 triggerVibration()
             }
        }
        
        viewModel.systemResetEvent.observe(this) { triggered ->
            if (triggered) {
                showSystemResetDialog()
                triggerVibration()
            }
        }
        
        viewModel.promotionEvent.observe(this) { eligible ->
            if (eligible) {
                showPromotionDialog()
                triggerVibration()
            }
        }
        
        viewModel.monarchEvent.observe(this) { eligible ->
            if (eligible) {
                showMonarchDialog()
                triggerVibration()
            }
        }
        
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                com.example.sololeveling.ui.common.AnimUtils.shake(binding.root)
                com.example.sololeveling.ui.common.SystemNotifier.show(this@MainActivity, "SYSTEM DOES NOT PERMIT RETREAT", com.example.sololeveling.ui.common.SystemNotificationView.Type.WARNING)
            }
        })
    }
    
    private fun showPenaltyDialog() {
        val dialog = android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_penalty)
        dialog.setCancelable(false) // No Escape
        
        dialog.findViewById<android.view.View>(R.id.btnConfirm).setOnClickListener {
            viewModel.resetPenaltyEvent()
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showSystemResetDialog() {
        val dialog = android.app.AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            .setTitle("SYSTEM RESET")
            .setMessage("") // Animate text
            .setCancelable(false)
            .setPositiveButton("ACCEPT") { _, _ -> 
                viewModel.resetSystemResetEvent()
            }
            .create()
            
        dialog.setOnShowListener {
            val messageView = dialog.findViewById<android.widget.TextView>(android.R.id.message)
            val fullText = "SURVIVAL FAILURE CONFIRMED.\n\ninitiating reset sequence...\n\nLEVEL -1\nSTATS -10%\nSHADOWS PENALIZED\n\nThe Player will now be returned to the beginning."
            com.example.sololeveling.ui.common.AnimUtils.typewriter(messageView, fullText)
        }
        dialog.show()
    }

    private fun showPromotionDialog() {
        val dialog = android.app.AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            .setTitle("SYSTEM NOTIFICATION")
            .setMessage("")
            .setCancelable(false)
            .setPositiveButton("ACCEPT PROMOTION") { _, _ -> 
                viewModel.promoteUser()
                viewModel.resetPromotionEvent()
                com.example.sololeveling.ui.common.SystemNotifier.show(this, "RANK INCREASED", com.example.sololeveling.ui.common.SystemNotificationView.Type.SUCCESS)
                triggerVibration()
            }
            .setNegativeButton("LATER") { _, _ -> viewModel.resetPromotionEvent() }
            .create()

        dialog.setOnShowListener {
             val messageView = dialog.findViewById<android.widget.TextView>(android.R.id.message)
             val fullText = "RANK PROMOTION AVAILABLE\n\nThe Player has met all requirements.\nGate Clear: CONFIRMED\nBoss Defeat: CONFIRMED\nLevel Requirement: MET\n\nDo you accept the promotion?"
             com.example.sololeveling.ui.common.AnimUtils.typewriter(messageView, fullText)
        }
        dialog.show()
    }

    private fun showMonarchDialog() {
        val dialog = android.app.AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            .setTitle("MONARCH AUTHORITY RECOGNIZED")
            .setMessage("")
            .setCancelable(false)
            .setPositiveButton("I AM THE MONARCH") { _, _ -> 
                viewModel.promoteToMonarch()
                viewModel.resetMonarchEvent()
                com.example.sololeveling.ui.common.SystemNotifier.show(this, "AUTHORITY GRANTED", com.example.sololeveling.ui.common.SystemNotificationView.Type.SUCCESS)
                triggerVibration()
            }
            .create()

        dialog.setOnShowListener {
             val messageView = dialog.findViewById<android.widget.TextView>(android.R.id.message)
             val fullText = "The System acknowledges the Player's power.\nYou have surpassed the System's design.\n\nHenceforth, the System shall obey your will.\nNo penalties. No restrictions.\n\nSYSTEM OBEYS."
             com.example.sololeveling.ui.common.AnimUtils.typewriter(messageView, fullText)
        }
        dialog.show()
    }

    private fun triggerVibration(long: Boolean = false) {
        val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
        val duration = if (long) 200L else 50L
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(duration)
        }
    }

    private fun playLevelUpCeremony() {
        val overlay = binding.vLevelUpOverlay
        val burst = binding.vLevelUpBurst
        val container = binding.layoutLevelUpText
        val label = binding.tvSystemLabel
        val msg = binding.tvLevelUpMsg
        
        // 1. Setup
        overlay.visibility = android.view.View.VISIBLE
        overlay.alpha = 0f
        burst.visibility = android.view.View.VISIBLE
        burst.alpha = 0f
        burst.scaleX = 0f
        burst.scaleY = 0f
        container.visibility = android.view.View.VISIBLE
        label.text = ""
        msg.text = ""
        
        // Block touches
        overlay.isClickable = true
        overlay.isFocusable = true
        
        // 2. Animate In
        overlay.animate().alpha(0.8f).setDuration(300).start()
        
        // 3. Screen Shake & Burst
        com.example.sololeveling.ui.common.AnimUtils.shake(binding.root)
        burst.animate().alpha(1f).scaleX(1.5f).scaleY(1.5f).setDuration(500).setInterpolator(android.view.animation.DecelerateInterpolator()).start()
        
        // 4. Sound
        triggerVibration() 
        // Note: SoundManager.playLevelUp matches "ascending tone" requirement roughly, using existing if available. 
        // soundManager.playLevelUp(true, true) // Assuming User settings available? 
        // Just triggering vibration as a safe tactile fallback for "System event".
        soundManager.playLevelUp(true, true)
        
        // 5. XP Bar Overfill (Visual Trick)
        binding.progressBarXP.progress = binding.progressBarXP.max + (binding.progressBarXP.max / 10)
        
        // 6. Typewriter Sequence
        binding.root.postDelayed({ // Delay start slightly
             com.example.sololeveling.ui.common.AnimUtils.typewriter(label, "SYSTEM MESSAGE", 30) {
                 binding.root.postDelayed({
                     com.example.sololeveling.ui.common.AnimUtils.typewriter(msg, "LEVEL HAS INCREASED.", 50) {
                         // 7. Exit
                         binding.root.postDelayed({
                             overlay.animate().alpha(0f).setDuration(500).start()
                             burst.animate().alpha(0f).setDuration(500).start()
                             container.animate().alpha(0f).setDuration(300).withEndAction {
                                 overlay.visibility = android.view.View.GONE
                                 overlay.isClickable = false
                                 burst.visibility = android.view.View.GONE
                                 container.visibility = android.view.View.GONE
                                 container.alpha = 1f // Reset
                                 // Snap XP bar back to normal (ViewModel triggers update anyway, but safe to sync)
                                 val user = viewModel.user.value
                                 if (user != null) {
                                     binding.progressBarXP.progress = user.currentXP.toInt()
                                 }
                             }.start()
                         }, 1500) // Read time
                     }
                 }, 300) // Delay between lines
             }
        }, 300)
    }

    private fun showStatsDialog() {
        val user = viewModel.user.value ?: return
        
        val dialog = android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_stats)
        dialog.setCancelable(false) // Strict Mode: Cannot back out without hitting Cancel or Confirm
        
        val tvAvailable = dialog.findViewById<android.widget.TextView>(R.id.tvAvailablePoints)
        val tvStr = dialog.findViewById<android.widget.TextView>(R.id.tvStrVal)
        val tvInt = dialog.findViewById<android.widget.TextView>(R.id.tvIntVal)
        val tvDisc = dialog.findViewById<android.widget.TextView>(R.id.tvDiscVal)
        val tvAwa = dialog.findViewById<android.widget.TextView>(R.id.tvAwaVal)
        val tvChr = dialog.findViewById<android.widget.TextView>(R.id.tvChrVal)
        val tvLuk = dialog.findViewById<android.widget.TextView>(R.id.tvLukVal)
        
        val btnConfirm = dialog.findViewById<android.view.View>(R.id.btnConfirmStats)
        val btnCancel = dialog.findViewById<android.view.View>(R.id.btnCancelStats)
        
        // Base values
        val baseStr = user.fitness
        val baseInt = user.knowledge
        val baseDisc = user.discipline
        val baseAwa = user.awareness
        val baseChr = user.charisma
        val baseLuk = user.luck
        
        // Local Allocation
        var addedStr = 0
        var addedInt = 0
        var addedDisc = 0
        var addedAwa = 0
        var addedChr = 0
        var addedLuk = 0
        var remaining = user.unspentPoints
        
        fun updateUI() {
            tvAvailable.text = "AVAILABLE POINTS: $remaining"
            tvStr.text = "${baseStr + addedStr}"
            tvInt.text = "${baseInt + addedInt}"
            tvDisc.text = "${baseDisc + addedDisc}" 
            tvAwa.text = "${baseAwa + addedAwa}"
            tvChr.text = "${baseChr + addedChr}"
            tvLuk.text = "${baseLuk + addedLuk}"
            
            // Visual feedback - Change color if modified
            tvStr.setTextColor(if (addedStr > 0) getColor(R.color.system_neon_blue) else android.graphics.Color.WHITE)
            tvInt.setTextColor(if (addedInt > 0) getColor(R.color.system_neon_blue) else android.graphics.Color.WHITE)
            tvDisc.setTextColor(if (addedDisc > 0) getColor(R.color.system_neon_blue) else android.graphics.Color.WHITE)
            tvAwa.setTextColor(if (addedAwa > 0) getColor(R.color.system_neon_blue) else android.graphics.Color.WHITE)
            tvChr.setTextColor(if (addedChr > 0) getColor(R.color.system_neon_blue) else android.graphics.Color.WHITE)
            tvLuk.setTextColor(if (addedLuk > 0) getColor(R.color.system_neon_blue) else android.graphics.Color.WHITE)
        }
        
        updateUI()
        
        // Setup Plus Buttons
        val buttons = listOf(
            Triple(R.id.btnStrPlus, { if(remaining > 0) { addedStr++; remaining-- } }, tvStr),
            Triple(R.id.btnIntPlus, { if(remaining > 0) { addedInt++; remaining-- } }, tvInt),
            Triple(R.id.btnDiscPlus, { if(remaining > 0) { addedDisc++; remaining-- } }, tvDisc),
            Triple(R.id.btnAwaPlus, { if(remaining > 0) { addedAwa++; remaining-- } }, tvAwa),
            Triple(R.id.btnChrPlus, { if(remaining > 0) { addedChr++; remaining-- } }, tvChr),
            Triple(R.id.btnLukPlus, { if(remaining > 0) { addedLuk++; remaining-- } }, tvLuk)
        )
        
        buttons.forEach { (id, action, targetView) ->
            dialog.findViewById<android.view.View>(id).setOnClickListener {
                if (remaining > 0) {
                    action()
                    triggerVibration() // Tactile click
                    com.example.sololeveling.ui.common.AnimUtils.pulse(targetView, 1.2f, 200, 0) // Visual Pulse
                    updateUI()
                }
            }
        }
        
        btnCancel.setOnClickListener {
            triggerVibration()
            dialog.dismiss()
        }
        
        btnConfirm.setOnClickListener {
            if (addedStr + addedInt + addedDisc + addedAwa + addedChr + addedLuk > 0) {
                // Ensure ViewModel supports 6 args or update this call
                viewModel.allocatePoints(addedStr, addedInt, addedDisc, addedAwa, addedChr, addedLuk)
                com.example.sololeveling.ui.common.SystemNotifier.show(this, "STATS UPDATED", com.example.sololeveling.ui.common.SystemNotificationView.Type.INFO)
                soundManager.playLevelUp(user.soundEnabled, user.hapticsEnabled) 
            }
            // Pulse effect to confirm
             com.example.sololeveling.ui.common.AnimUtils.pulse(binding.statsContainer, 1.1f, 300, 0)
            dialog.dismiss()
        }
        
        dialog.show()
    }
}