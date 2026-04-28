package com.example.sololeveling.ui.gate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sololeveling.data.entity.GateEntity
import com.example.sololeveling.databinding.ItemGateBinding
import com.example.sololeveling.R

class GateAdapter(private val onClick: (GateEntity) -> Unit) :
    ListAdapter<GateEntity, GateAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemGateBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(gate: GateEntity) {
            binding.tvName.text = gate.name
            binding.tvDesc.text = gate.description
            binding.tvLevelReq.text = "LVL ${gate.requiredLevel}"
            
            // Status Logic
            if (gate.isCleared) {
                binding.tvStatus.text = "[ CLEARED ]"
                binding.tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_green_light))
                binding.root.alpha = 0.6f
            } else if (gate.isFailed && System.currentTimeMillis() < gate.cooldownEnd) {
                binding.tvStatus.text = "[ LOCKED (COOLDOWN) ]"
                binding.tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_red_dark))
                binding.root.alpha = 0.4f
                binding.root.isEnabled = false
            } else if (gate.isActive) {
                binding.tvStatus.text = "[ ACTIVE - IN PROGRESS ]"
                 binding.tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_orange_light))
                 binding.root.alpha = 1.0f
            } else {
                binding.tvStatus.text = "[ AVAILABLE ]"
                binding.tvStatus.setTextColor(binding.root.context.getColor(R.color.system_neon_blue))
                binding.root.alpha = 1.0f
            }

            // Red Gate Highlight
            if (gate.name.contains("Red", ignoreCase = true)) {
                 binding.tvName.setTextColor(binding.root.context.getColor(android.R.color.holo_red_light))
                 binding.root.setBackgroundColor(0xFF220000.toInt()) // subtle red bg
            } else {
                 binding.tvName.setTextColor(binding.root.context.getColor(R.color.system_neon_blue))
                 binding.root.setBackgroundResource(R.drawable.bg_system_card)
            }

            binding.root.setOnClickListener {
                if (!gate.isCleared && (System.currentTimeMillis() >= gate.cooldownEnd || gate.isActive)) {
                    onClick(gate)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<GateEntity>() {
        override fun areItemsTheSame(oldItem: GateEntity, newItem: GateEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GateEntity, newItem: GateEntity) = oldItem == newItem
    }
}
