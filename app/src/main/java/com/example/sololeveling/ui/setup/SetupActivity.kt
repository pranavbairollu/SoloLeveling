package com.example.sololeveling.ui.setup

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.sololeveling.R
import com.example.sololeveling.databinding.ActivitySetupBinding
import com.example.sololeveling.data.db.SystemDatabase
import com.example.sololeveling.data.repository.*
import com.example.sololeveling.data.pref.UserPreferences
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private lateinit var navController: NavController
    lateinit var viewModel: SetupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel (Manual DI for Step 3, in real app use Hilt/Koin)
        val database = SystemDatabase.getDatabase(this)
        val userRepo = UserRepository(database.userDao())
        val questRepo = QuestRepository(database.questDao())
        val gateRepo = GateRepository(database.gateDao())
        val bossRepo = BossRepository(database.bossDao(), database.gateDao())
        val shadowRepo = ShadowRepository(database.shadowDao(), database.questDao())
        val monarchRepo = MonarchRepository(database.monarchDao())
        val prefs = UserPreferences(this) // Pass if needed to ViewModel, but Logic says ViewModel handles it via calls. 
        
        // Factory
        // Ideally use a factory, but here we can't change ViewModel constructor easily without boilerplate.
        // Assuming we can create a simple factory or modify ViewModel to accept dependencies.
        // Since I cannot change ViewModel logic (constraint), I must assume a Factory exists OR I create one here.
        // Constraint: "USE existing SetupViewModel... DO NOT change logic" -> Does not say "DO NOT add factory".
        
        val factory = object : androidx.lifecycle.ViewModelProvider.Factory {
             override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SetupViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return SetupViewModel(userRepo, questRepo, gateRepo, bossRepo, shadowRepo, monarchRepo) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        viewModel = ViewModelProvider(this, factory)[SetupViewModel::class.java]

        // Setup Navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_setup) as NavHostFragment
        navController = navHostFragment.navController

        setupNavigationListeners()
    }

    private fun setupNavigationListeners() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.setupWelcomeFragment -> {
                    binding.progressIndicator.visibility = View.INVISIBLE
                    binding.tvStepTitle.visibility = View.INVISIBLE
                    binding.btnBack.visibility = View.GONE
                    binding.btnNext.visibility = View.GONE
                }
                R.id.roleSelectionFragment -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                    binding.tvStepTitle.visibility = View.VISIBLE
                    updateStep(1, "Step 1: Role Selection")
                    binding.btnBack.visibility = View.VISIBLE
                    binding.btnNext.visibility = View.VISIBLE
                    binding.btnNext.text = "Confirm Role"
                }
                R.id.setupContractFragment -> {
                    updateStep(2, "Step 2: The Contract")
                    binding.btnBack.visibility = View.VISIBLE
                    binding.btnNext.visibility = View.GONE // Handled by Fragment (Accept Authority)
                }
                R.id.setupEditorFragment -> {
                    updateStep(3, "Step 3: Review & Finalize")
                    binding.btnBack.visibility = View.VISIBLE
                    binding.btnNext.visibility = View.VISIBLE
                    binding.btnNext.text = "Complete Awakening"
                }
            }
        }

        binding.btnNext.setOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.roleSelectionFragment -> {
                    // Logic check: Ensure a class is actually selected? 
                    // ViewModel has a default, but we could enforce interaction.
                    navController.navigate(R.id.action_roleSelection_to_contract)
                }
                R.id.setupEditorFragment -> {
                    viewModel.confirmSetup()
                }
            }
        }

        binding.btnBack.setOnClickListener {
            navController.navigateUp()
        }
        
        observeSetupCompletion()
    }
    
    private fun observeSetupCompletion() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.setupState.collect { state ->
                    when (state) {
                        is SetupState.Success -> {
                            val intent = android.content.Intent(this@SetupActivity, com.example.sololeveling.MainActivity::class.java)
                            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            finish()
                        }
                        is SetupState.Error -> {
                            // Handled by fragment
                        }
                        is SetupState.Saving -> {
                            binding.btnNext.isEnabled = false
                            binding.btnBack.isEnabled = false
                        }
                        else -> {
                            binding.btnNext.isEnabled = true
                            binding.btnBack.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    private fun updateStep(step: Int, title: String) {
        binding.progressIndicator.progress = step
        binding.tvStepTitle.text = title
    }
}
