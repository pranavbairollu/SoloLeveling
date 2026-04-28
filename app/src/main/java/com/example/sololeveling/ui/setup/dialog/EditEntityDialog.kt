package com.example.sololeveling.ui.setup.dialog

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import com.example.sololeveling.databinding.DialogEditEntityBinding
import com.example.sololeveling.data.entity.*

class EditEntityDialog(private val context: Context) {

    private fun setupValidation(binding: DialogEditEntityBinding, dialog: AlertDialog) {
        val validator = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = binding.etName.text.toString().trim()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = text.isNotEmpty()
            }
        }
        binding.etName.addTextChangedListener(validator)
    }

    fun showQuestEdit(quest: QuestEntity, onSave: (QuestEntity) -> Unit) {
        val binding = DialogEditEntityBinding.inflate(LayoutInflater.from(context))
        
        binding.tilName.hint = "Quest Title"
        binding.etName.setText(quest.title)
        
        binding.containerQuestReq.visibility = View.VISIBLE
        binding.etReqTarget.setText(quest.requirementTarget.toString())
        binding.etReqUnit.setText(quest.requirementUnit)
        
        binding.containerQuestMeta.visibility = View.VISIBLE
        binding.etDifficulty.setText(quest.difficulty)
        binding.etLinkedStat.setText(quest.linkedStat)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Edit Quest")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = binding.etName.text.toString().trim()
                val target = binding.etReqTarget.text.toString().toIntOrNull() ?: 1
                val unit = binding.etReqUnit.text.toString()
                val diff = binding.etDifficulty.text.toString()
                val stat = binding.etLinkedStat.text.toString()
                
                onSave(quest.copy(
                    title = newTitle,
                    requirementTarget = target,
                    requirementUnit = unit,
                    difficulty = diff,
                    linkedStat = stat
                ))
            }
            .setNegativeButton("Cancel", null)
            .create()
            
        dialog.show()
        setupValidation(binding, dialog)
    }

    fun showGateEdit(gate: GateEntity, onSave: (GateEntity) -> Unit) {
        val binding = DialogEditEntityBinding.inflate(LayoutInflater.from(context))
        
        binding.tilName.hint = "Gate Name"
        binding.etName.setText(gate.name)
        
        binding.tilDuration.visibility = View.VISIBLE
        binding.etDuration.setText(gate.durationDays.toString())

        val dialog = AlertDialog.Builder(context)
            .setTitle("Edit Gate")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val newName = binding.etName.text.toString().trim()
                val duration = binding.etDuration.text.toString().toIntOrNull() ?: 7
                
                onSave(gate.copy(
                    name = newName,
                    durationDays = duration
                ))
            }
            .setNegativeButton("Cancel", null)
            .create()
            
        dialog.show()
        setupValidation(binding, dialog)
    }

    fun showBossEdit(boss: BossEntity, onSave: (BossEntity) -> Unit) {
        val binding = DialogEditEntityBinding.inflate(LayoutInflater.from(context))
        
        binding.tilName.hint = "Boss Name"
        binding.etName.setText(boss.name)
        
        binding.tilRank.visibility = View.VISIBLE
        binding.etRank.setText(boss.rank)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Edit Boss")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val newName = binding.etName.text.toString().trim()
                val rank = binding.etRank.text.toString()
                
                onSave(boss.copy(
                    name = newName,
                    rank = rank
                ))
            }
            .setNegativeButton("Cancel", null)
            .create()
            
        dialog.show()
        setupValidation(binding, dialog)
    }

    fun showShadowEdit(shadow: ShadowEntity, onSave: (ShadowEntity) -> Unit) {
        val binding = DialogEditEntityBinding.inflate(LayoutInflater.from(context))
        
        binding.tilName.hint = "Shadow Name"
        binding.etName.setText(shadow.name)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Edit Shadow")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val newName = binding.etName.text.toString().trim()
                onSave(shadow.copy(name = newName))
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
        setupValidation(binding, dialog)
    }

    fun showMonarchEdit(monarch: MonarchEntity, onSave: (MonarchEntity) -> Unit) {
        val binding = DialogEditEntityBinding.inflate(LayoutInflater.from(context))
        
        binding.tilName.hint = "Monarch Title"
        binding.etName.setText(monarch.title)
        
        binding.tilVictory.visibility = View.VISIBLE
        binding.etVictory.setText(monarch.victoryCondition)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Edit Monarch")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = binding.etName.text.toString().trim()
                val victory = binding.etVictory.text.toString()
                
                onSave(monarch.copy(
                    title = newTitle,
                    victoryCondition = victory
                ))
            }
            .setNegativeButton("Cancel", null)
            .create()
            
        dialog.show()
        setupValidation(binding, dialog)
    }
}
