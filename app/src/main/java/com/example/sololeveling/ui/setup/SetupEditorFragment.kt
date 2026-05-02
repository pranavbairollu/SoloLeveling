package com.example.sololeveling.ui.setup

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sololeveling.databinding.FragmentSetupEditorBinding
import com.example.sololeveling.ui.setup.adapter.SetupEntityAdapter
import com.example.sololeveling.ui.setup.dialog.EditEntityDialog
import com.example.sololeveling.data.entity.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupEditorFragment : Fragment() {

    private var _binding: FragmentSetupEditorBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SetupViewModel
    private lateinit var adapter: SetupEntityAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSetupEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (requireActivity() as SetupActivity).viewModel

        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = SetupEntityAdapter(
            onHeaderInfoClick = { title -> showInfoDialog(title) },
            onEntityClick = { entity -> showEditDialog(entity) }
        )
        binding.rvEntities.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEntities.adapter = adapter
    }

    private fun observeData() {
        // Observe State (Success/Error)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                 viewModel.setupState.collectLatest { state ->
                    when (state) {
                        is SetupState.Success -> {
                            android.widget.Toast.makeText(requireContext(), "System Awakened.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        is SetupState.Error -> {
                            android.widget.Toast.makeText(requireContext(), "Error: ${state.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                        else -> {} // Idle/Saving handled by Activity loading indicator mostly
                    }
                }
            }
        }

        // Observe Data Lists
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.quests.collectLatest { updateList() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.gates.collectLatest { updateList() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bosses.collectLatest { updateList() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shadows.collectLatest { updateList() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monarch.collectLatest { updateList() }
        }
    }
    
    private fun updateList() {
        adapter.setData(
            viewModel.quests.value,
            viewModel.gates.value,
            viewModel.bosses.value,
            viewModel.shadows.value,
            viewModel.monarch.value
        )
    }

    private fun showInfoDialog(title: String) {
        val message = when (title) {
            "Quests" -> "Your daily routine. Tasks you must complete to grow stronger."
            "Gates" -> "Time-bound challenges. Maintain streaks to clear them."
            "Bosses" -> "Major milestones. Require specific stats to defeat."
            "Shadows" -> "Mentors or systems that provide passive boosts."
            "Monarch" -> "Your ultimate goal. The final condition for victory."
            else -> ""
        }
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showEditDialog(entity: Any) {
        val dialog = EditEntityDialog(requireContext())
        when (entity) {
            is QuestEntity -> dialog.showQuestEdit(entity) { updated -> viewModel.updateQuest(updated) }
            is GateEntity -> dialog.showGateEdit(entity) { updated -> viewModel.updateGate(updated) }
            is BossEntity -> dialog.showBossEdit(entity) { updated -> viewModel.updateBoss(updated) }
            is ShadowEntity -> dialog.showShadowEdit(entity) { updated -> viewModel.updateShadow(updated) }
            is MonarchEntity -> dialog.showMonarchEdit(entity) { updated -> viewModel.updateMonarch(updated) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
