package com.example.sololeveling.ui.shadow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sololeveling.R
import com.example.sololeveling.data.entity.ShadowEntity
import com.example.sololeveling.databinding.ItemShadowBinding

class ShadowAdapter(private val onShadowClick: (ShadowEntity) -> Unit) :
    ListAdapter<ShadowEntity, ShadowAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemShadowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(shadow: ShadowEntity) {
            binding.tvName.text = shadow.name
            binding.tvBonus.text = "Bonus: ${shadow.boostedStat}"
            
            if (shadow.isActive) {
                binding.tvStatus.text = "[ ACTIVE ]"
                binding.tvStatus.setTextColor(binding.root.context.getColor(R.color.system_neon_purple))
                binding.root.alpha = 1.0f
                binding.ivIcon.setColorFilter(binding.root.context.getColor(R.color.system_neon_purple))
            } else {
                binding.tvStatus.text = "[ LOCKED - TAP TO EXTRACT ]"
                binding.tvStatus.setTextColor(binding.root.context.getColor(android.R.color.darker_gray))
                binding.root.alpha = 0.6f
                 binding.ivIcon.setColorFilter(binding.root.context.getColor(android.R.color.darker_gray))
            }

            binding.root.setOnClickListener {
                onShadowClick(shadow)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShadowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ShadowEntity>() {
        override fun areItemsTheSame(oldItem: ShadowEntity, newItem: ShadowEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ShadowEntity, newItem: ShadowEntity) = oldItem == newItem
    }
}
