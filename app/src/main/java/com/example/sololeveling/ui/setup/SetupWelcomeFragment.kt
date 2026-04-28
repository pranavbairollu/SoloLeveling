package com.example.sololeveling.ui.setup

import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import com.example.sololeveling.R
import com.example.sololeveling.databinding.FragmentSetupWelcomeBinding

import androidx.navigation.fragment.findNavController

class SetupWelcomeFragment : Fragment(R.layout.fragment_setup_welcome) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSetupWelcomeBinding.bind(view)

        // Typewriter Animation Simulation
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val message = "You have been chosen by the System.\n\nYour life will now change.\n\nAre you ready to accept your new reality?"
        
        binding.tvWelcomeMsg.text = ""
        com.example.sololeveling.ui.common.AnimUtils.typewriter(binding.tvWelcomeMsg, message, 50) {
            binding.btnStart.visibility = View.VISIBLE
            val anim = AlphaAnimation(0f, 1f)
            anim.duration = 1000
            binding.btnStart.startAnimation(anim)
        }

        binding.btnStart.setOnClickListener {
            // Navigate to Contract (Name Entry)
            findNavController().navigate(R.id.action_welcome_to_contract)
        }
    }
}
