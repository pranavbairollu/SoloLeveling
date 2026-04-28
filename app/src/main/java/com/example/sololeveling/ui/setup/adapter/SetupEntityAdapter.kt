package com.example.sololeveling.ui.setup.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sololeveling.R
import com.example.sololeveling.data.entity.*

class SetupEntityAdapter(
    private val onHeaderInfoClick: (String) -> Unit,
    private val onEntityClick: (Any) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Any>()

    fun setData(
        quests: List<QuestEntity>,
        gates: List<GateEntity>,
        bosses: List<BossEntity>,
        shadows: List<ShadowEntity>,
        monarch: MonarchEntity?
    ) {
        items.clear()
        
        // Quests
        items.add(HeaderItem("Quests"))
        items.addAll(quests)
        
        // Gates
        items.add(HeaderItem("Gates"))
        items.addAll(gates)
        
        // Bosses
        items.add(HeaderItem("Bosses"))
        items.addAll(bosses)
        
        // Shadows
        items.add(HeaderItem("Shadows"))
        items.addAll(shadows)
        
        // Monarch
        items.add(HeaderItem("Monarch"))
        if (monarch != null) {
            items.add(monarch)
        }
        
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is HeaderItem) TYPE_HEADER else TYPE_ENTITY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            val view = inflater.inflate(R.layout.item_setup_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_setup_entity, parent, false)
            EntityViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is HeaderViewHolder && item is HeaderItem) {
            holder.bind(item)
        } else if (holder is EntityViewHolder) {
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvHeaderTitle)
        private val iconInfo: View = itemView.findViewById(R.id.ivInfo)

        fun bind(header: HeaderItem) {
            tvTitle.text = header.title
            iconInfo.setOnClickListener { onHeaderInfoClick(header.title) }
        }
    }

    inner class EntityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvEntityTitle)
        private val tvDetails: TextView = itemView.findViewById(R.id.tvEntityDetails)

        fun bind(item: Any) {
            itemView.setOnClickListener { onEntityClick(item) }
            
            when (item) {
                is QuestEntity -> {
                    tvTitle.text = item.title
                    tvDetails.text = "[${item.difficulty}] ${item.requirementTarget} ${item.requirementUnit} (${item.linkedStat})"
                }
                is GateEntity -> {
                    tvTitle.text = item.name
                    tvDetails.text = "${item.durationDays} Days | ${item.description}"
                }
                is BossEntity -> {
                    tvTitle.text = item.name
                    tvDetails.text = "Rank ${item.rank} | ${item.description}"
                }
                is ShadowEntity -> {
                    tvTitle.text = item.name
                    tvDetails.text = "Boosts: ${item.boostedStat} (x${item.boostMultiplier})"
                }
                is MonarchEntity -> {
                    tvTitle.text = item.title
                    tvDetails.text = "Win: ${item.victoryCondition}"
                }
            }
        }
    }

    data class HeaderItem(val title: String)

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ENTITY = 1
    }
}
