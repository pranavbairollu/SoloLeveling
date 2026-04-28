package com.example.sololeveling.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sololeveling.databinding.FragmentRoleSelectionBinding
import com.example.sololeveling.logic.SupportedClass
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RoleSelectionFragment : Fragment() {

    private var _binding: FragmentRoleSelectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SetupViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRoleSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (requireActivity() as SetupActivity).viewModel

        setupClickListeners()
        observeSelection()
    }

    private fun setupClickListeners() {
        binding.cardStudent.setOnClickListener { viewModel.selectClass(SupportedClass.STUDENT) }
        binding.cardAthlete.setOnClickListener { viewModel.selectClass(SupportedClass.ATHLETE) }
        binding.cardExecutive.setOnClickListener { viewModel.selectClass(SupportedClass.EXECUTIVE) }
        binding.cardCustom.setOnClickListener { viewModel.selectClass(SupportedClass.CUSTOM) }
    }

    private fun observeSelection() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedClass.collectLatest { selected ->
                resetCards()
                when (selected) {
                    SupportedClass.STUDENT -> highlightCard(binding.cardStudent)
                    SupportedClass.ATHLETE -> highlightCard(binding.cardAthlete)
                    SupportedClass.EXECUTIVE -> highlightCard(binding.cardExecutive)
                    SupportedClass.CUSTOM -> highlightCard(binding.cardCustom)
                }
            }
        }
    }

    private fun highlightCard(card: MaterialCardView) {
        card.strokeColor = resources.getColor(android.R.color.holo_blue_light, null)
        card.strokeWidth = 4
    }

    private fun resetCards() {
        // Reset stroke to default
        val defaultColor = resources.getColor(android.R.color.darker_gray, null)
        val defaultWidth = 1
        
        binding.cardStudent.strokeColor = defaultColor
        binding.cardStudent.strokeWidth = defaultWidth
        
        binding.cardAthlete.strokeColor = defaultColor
        binding.cardAthlete.strokeWidth = defaultWidth
        
        binding.cardExecutive.strokeColor = defaultColor
        binding.cardExecutive.strokeWidth = defaultWidth
        
        binding.cardCustom.strokeColor = defaultColor
        binding.cardCustom.strokeWidth = defaultWidth
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
