package com.example.sololeveling.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sololeveling.R
import com.example.sololeveling.data.entity.QuestEntity
import com.example.sololeveling.databinding.ItemQuestBinding

class QuestAdapter(private val onQuestClicked: (QuestEntity) -> Unit) :
    ListAdapter<QuestEntity, QuestAdapter.QuestViewHolder>(QuestDiffCallback()) {

    inner class QuestViewHolder(private val binding: ItemQuestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(quest: QuestEntity) {
            binding.tvTitle.text = quest.title.uppercase()
            binding.tvDesc.text = "[ SYSTEM ASSIGNED QUEST ]"
            binding.tvReward.text = "XP+${quest.xpReward} ${quest.linkedStat}"

            if (quest.isCompleted) {
                binding.tvStatus.text = "[ COMPLETED ]"
                binding.tvStatus.setTextColor(Color.GREEN)
                binding.root.setOnClickListener(null)
                binding.root.alpha = 0.5f
            } else {
                binding.tvStatus.text = "[ INCOMPLETE ]"
                binding.tvStatus.setTextColor(Color.RED) 
                binding.root.alpha = 1.0f
                binding.root.setOnClickListener { onQuestClicked(quest) }
            }
            
            // Shadow Styling
            if (quest.linkedStat.startsWith("SHADOW_")) {
                binding.tvTitle.setTextColor(Color.parseColor("#D500F9")) // Neon Purple
            } else {
                binding.tvTitle.setTextColor(Color.parseColor("#00E5FF")) // Neon Blue
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val binding = ItemQuestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class QuestDiffCallback : DiffUtil.ItemCallback<QuestEntity>() {
        override fun areItemsTheSame(oldItem: QuestEntity, newItem: QuestEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: QuestEntity, newItem: QuestEntity) =
            oldItem == newItem
    }
}
