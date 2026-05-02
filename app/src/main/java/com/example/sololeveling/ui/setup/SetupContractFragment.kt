package com.example.sololeveling.ui.setup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sololeveling.MainActivity
import com.example.sololeveling.databinding.FragmentSetupContractBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import androidx.navigation.fragment.findNavController

class SetupContractFragment : Fragment() {

    private var _binding: FragmentSetupContractBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SetupViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSetupContractBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (requireActivity() as SetupActivity).viewModel

        setupContextUI()
        setupInputValidation()
        
        binding.btnAccept.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isNotEmpty() && binding.cbAcceptRules.isChecked) {
                viewModel.setPlayerName(name)
                // Navigate to Next Step (Review & Finalize)
                findNavController().navigate(com.example.sololeveling.R.id.action_contract_to_setupEditor)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitWarning()
            }
        })
    }

    private fun setupContextUI() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.selectedClass.collectLatest { 
                    binding.tvSelectedClass.text = it.name
                    val monarch = viewModel.monarch.value
                    binding.tvMonarchGoal.text = monarch?.victoryCondition ?: "Complete The System"
                    
                    val qCount = viewModel.quests.value.size
                    val gCount = viewModel.gates.value.size
                    val bCount = viewModel.bosses.value.size
                    binding.tvEntityCounts.text = "Quests: $qCount | Gates: $gCount | Bosses: $bCount"
                }
            }
        }
    }

    private fun showExitWarning() {
        val dialogView = android.view.LayoutInflater.from(requireContext()).inflate(com.example.sololeveling.R.layout.dialog_system_warning, null)
        
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        dialogView.findViewById<android.view.View>(com.example.sololeveling.R.id.btnPositive).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<android.view.View>(com.example.sololeveling.R.id.btnNegative).setOnClickListener {
            requireActivity().finishAffinity()
        }
        
        dialog.show()
    }

    private fun setupInputValidation() {
        binding.btnAccept.isEnabled = false 
        
        val validate = {
            val name = binding.etName.text.toString().trim()
            binding.btnAccept.isEnabled = name.isNotEmpty() && binding.cbAcceptRules.isChecked
        }

        binding.etName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validate()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.cbAcceptRules.setOnCheckedChangeListener { _, _ -> validate() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
