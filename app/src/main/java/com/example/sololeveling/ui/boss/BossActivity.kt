package com.example.sololeveling.ui.boss

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.sololeveling.R
import com.example.sololeveling.SoloLevelingApp
import com.example.sololeveling.data.entity.BossEntity
import com.example.sololeveling.databinding.ActivityBossBinding
import com.example.sololeveling.databinding.ItemBossBinding
import com.example.sololeveling.ui.common.SystemViewModelFactory

class BossActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBossBinding
    private lateinit var viewModel: BossViewModel
    private var adapter: BossAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBossBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as SoloLevelingApp
        val factory = SystemViewModelFactory(app.userRepository, app.questRepository, app.gateRepository, app.bossRepository, app.shadowRepository)
        viewModel = ViewModelProvider(this, factory)[BossViewModel::class.java]

        setupAdapter()
        setupObservers()
    }

    private fun setupAdapter() {
        adapter = BossAdapter { boss ->
            if (boss.isActive) {
                showResolutionDialog(boss)
            } else {
                viewModel.checkBossQualification(boss)
            }
        }
        binding.rvBosses.layoutManager = LinearLayoutManager(this)
        binding.rvBosses.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.allBosses.observe(this) { bosses ->
            adapter?.submitList(bosses)
        }
        
        viewModel.messageEvent.observe(this) { msg ->
            com.example.sololeveling.ui.common.SystemNotifier.show(this, msg, com.example.sololeveling.ui.common.SystemNotificationView.Type.INFO)
        }
        
        viewModel.bossResultEvent.observe(this) { result ->
            when (result) {
                is BossViewModel.BossResult.QualificationInfo -> {
                    showConfirmationDialog(result.boss, result.q, result.missingStats)
                }
                else -> {}
            }
        }

        viewModel.extractionNavEvent.observe(this) { shadowId ->
            shadowId?.let {
                val intent = android.content.Intent(this, com.example.sololeveling.ui.shadow.ShadowActivity::class.java)
                intent.putExtra("SHADOW_ID", it)
                startActivity(intent)
            }
        }
    }
    
    private fun showConfirmationDialog(boss: BossEntity, q: BossViewModel.Qualification, missing: List<String>) {
        val dialog = android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_boss_confirm)
        dialog.setCancelable(true)
        
        val tvTitle = dialog.findViewById<android.widget.TextView>(R.id.tvDialogTitle)
        val tvName = dialog.findViewById<android.widget.TextView>(R.id.tvBossName)
        val tvDetails = dialog.findViewById<android.widget.TextView>(R.id.tvWarningMsg)
        val tvStats1 = dialog.findViewById<android.widget.TextView>(R.id.tvStats1)
        val tvStats2 = dialog.findViewById<android.widget.TextView>(R.id.tvStats2)
        val btnEngage = dialog.findViewById<android.widget.Button>(R.id.btnEngage)
        val btnRetreat = dialog.findViewById<android.widget.TextView>(R.id.btnRetreat)
        
        tvName.text = boss.name
        
        val warningText = when(q) {
            BossViewModel.Qualification.QUALIFIED -> "CONDITION: OPTIMAL.\nThe system has verified your growth. Victory is highly probable."
            BossViewModel.Qualification.HIGH_RISK -> "WARNING: HIGH RISK.\nMissing requirements: ${missing.joinToString(", ")}.\nProceeding will likely result in failure."
            BossViewModel.Qualification.UNQUALIFIED -> "DANGER: ABSOLUTE WEAKNESS.\nRequirements not met. Entry is suicidal.\nPenalties will be severe."
            BossViewModel.Qualification.ON_COOLDOWN -> "SYSTEM RECOVERY IN PROGRESS.\nBoss is currently unavailable."
            BossViewModel.Qualification.NO_GATES -> "ACCESS DENIED.\nNo Gate completions detected. You are not qualified to face a Boss."
        }
        
        tvDetails.text = warningText
        if (q == BossViewModel.Qualification.UNQUALIFIED || q == BossViewModel.Qualification.NO_GATES) {
             tvTitle.text = "ACCESS DENIED"
             btnEngage.isEnabled = false
             btnEngage.alpha = 0.5f
        } else if (q == BossViewModel.Qualification.HIGH_RISK) {
             tvTitle.text = "CRITICAL WARNING"
             tvTitle.setTextColor(android.graphics.Color.YELLOW)
        }

        tvStats1.text = "STR: ${boss.requiredFitness}  |  INT: ${boss.requiredKnowledge}  |  VIT: ${boss.requiredDiscipline}"
        tvStats2.text = "AGI: ${boss.requiredAwareness}  |  CHR: ${boss.requiredCharisma}   |  LUK: ${boss.requiredLuck}"

        btnEngage.setOnClickListener {
            viewModel.startRaid(boss)
            dialog.dismiss()
        }
        
        btnRetreat.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun showResolutionDialog(boss: BossEntity) {
        AlertDialog.Builder(this)
            .setTitle("RAID IN PROGRESS")
            .setMessage("Have you completed the real-world milestone: '${boss.name}'?")
            .setPositiveButton("CONFIRM VICTORY") { _, _ ->
                viewModel.resolveRaid(boss, true)
            }
            .setNegativeButton("ADMIT DEFEAT") { _, _ ->
                viewModel.resolveRaid(boss, false)
            }
            .setNeutralButton("STILL FIGHTING", null)
            .show()
    }
}

class BossAdapter(private val onClick: (BossEntity) -> Unit) :
    ListAdapter<BossEntity, BossAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemBossBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(boss: BossEntity) {
            binding.tvName.text = boss.name
            binding.tvDesc.text = boss.description
            binding.tvLevelReq.text = "RANK ${boss.rank} | LVL ${boss.requiredLevel}"
            
            val imageRes = when {
                boss.name.contains("Igris", ignoreCase = true) -> R.mipmap.ic_launcher_round
                boss.name.contains("Tank", ignoreCase = true) -> R.mipmap.ic_launcher_round
                else -> R.mipmap.ic_launcher_round
            }
            binding.ivBossImage.setImageResource(imageRes)
            
            when {
                !boss.isUnlocked -> {
                    binding.tvStatus.text = "[ RANK LOCKED ]"
                    binding.tvStatus.setTextColor(android.graphics.Color.GRAY)
                    binding.root.alpha = 0.3f
                    binding.ivBossImage.setColorFilter(android.graphics.Color.BLACK)
                    binding.root.setOnClickListener(null)
                }
                boss.isDefeated -> {
                    val dateStr = android.text.format.DateFormat.format("MMM dd, yyyy", boss.defeatDate)
                    binding.tvStatus.text = "[ DEFEATED: $dateStr ]"
                    binding.tvStatus.setTextColor(android.graphics.Color.GREEN)
                    binding.root.alpha = 0.6f
                    binding.ivBossImage.clearColorFilter()
                    binding.root.setOnClickListener(null)
                }
                boss.isActive -> {
                    binding.tvStatus.text = "[ RAID ACTIVE ]"
                    binding.tvStatus.setTextColor(android.graphics.Color.CYAN)
                    binding.root.alpha = 1.0f
                    binding.ivBossImage.clearColorFilter()
                    binding.root.setOnClickListener { onClick(boss) }
                }
                System.currentTimeMillis() < boss.cooldownUntil -> {
                    binding.tvStatus.text = "[ COOLDOWN ]"
                    binding.tvStatus.setTextColor(android.graphics.Color.RED)
                    binding.root.alpha = 0.5f
                    binding.ivBossImage.clearColorFilter()
                    binding.root.setOnClickListener(null)
                }
                else -> {
                    binding.tvStatus.text = "[ AVAILABLE ]"
                    binding.tvStatus.setTextColor(android.graphics.Color.RED)
                    binding.root.alpha = 1.0f
                    binding.ivBossImage.clearColorFilter()
                    binding.root.setOnClickListener { onClick(boss) }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBossBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<BossEntity>() {
        override fun areItemsTheSame(oldItem: BossEntity, newItem: BossEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BossEntity, newItem: BossEntity) = oldItem == newItem
    }
}
