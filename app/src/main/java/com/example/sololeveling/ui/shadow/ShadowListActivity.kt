package com.example.sololeveling.ui.shadow

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sololeveling.SoloLevelingApp
import com.example.sololeveling.databinding.ActivityShadowListBinding
import com.example.sololeveling.ui.common.SystemViewModelFactory

class ShadowListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShadowListBinding
    private lateinit var viewModel: ShadowViewModel
    private val adapter = ShadowAdapter { shadow ->
        if (!shadow.isActive) {
             val intent = Intent(this, ShadowActivity::class.java).apply {
                putExtra("SHADOW_ID", shadow.id)
            }
            startActivity(intent)
        } else {
             Toast.makeText(this, "${shadow.name} is already serving you.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShadowListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as SoloLevelingApp
        val factory = SystemViewModelFactory(app.userRepository, app.questRepository, app.gateRepository, app.bossRepository, app.shadowRepository)
        viewModel = ViewModelProvider(this, factory)[ShadowViewModel::class.java]

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.rvShadows.layoutManager = LinearLayoutManager(this)
        binding.rvShadows.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.allShadows.observe(this) { shadows ->
            adapter.submitList(shadows)
        }
    }
}
