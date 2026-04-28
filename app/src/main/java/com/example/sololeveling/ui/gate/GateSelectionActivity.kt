package com.example.sololeveling.ui.gate

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sololeveling.SoloLevelingApp
import com.example.sololeveling.databinding.ActivityGateSelectionBinding
import com.example.sololeveling.ui.common.SystemViewModelFactory

class GateSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGateSelectionBinding
    private lateinit var viewModel: GateViewModel
    private val adapter = GateAdapter { gate ->
        val intent = Intent(this, GateActivity::class.java).apply {
            putExtra("GATE_ID", gate.id)
            putExtra("IS_RED_GATE", gate.name.contains("Red", ignoreCase = true))
        }
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGateSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as SoloLevelingApp
        val factory = SystemViewModelFactory(app.userRepository, app.questRepository, app.gateRepository, app.bossRepository, app.shadowRepository)
        viewModel = ViewModelProvider(this, factory)[GateViewModel::class.java]

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.rvGates.layoutManager = LinearLayoutManager(this)
        binding.rvGates.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.allGates.observe(this) { gates ->
            adapter.submitList(gates)
        }
    }
}
