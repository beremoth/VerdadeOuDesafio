package com.example.verdadeoudesafio.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.verdadeoudesafio.data.entity.DesafioEntity // Import DesafioEntity
import com.example.verdadeoudesafio.databinding.ItemAdminBinding
import com.example.verdadeoudesafio.databinding.ItemAdminHeaderBinding

// Sealed class agora usa TextLevelItem
sealed class AdminDisplayItem {
    data class Header(val level: Int) : AdminDisplayItem()
    data class Item(val textLevelItem: TextLevelItem) : AdminDisplayItem()
}

class AdminItemAdapter(
    private var displayItems: MutableList<AdminDisplayItem> = mutableListOf(),
    // Callbacks recebem TextLevelItem
    private val onEdit: (TextLevelItem) -> Unit,
    private val onDelete: (TextLevelItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    inner class ItemViewHolder(val binding: ItemAdminBinding) : RecyclerView.ViewHolder(binding.root)
    inner class HeaderViewHolder(val binding: ItemAdminHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return when (displayItems[position]) {
            is AdminDisplayItem.Header -> VIEW_TYPE_HEADER
            is AdminDisplayItem.Item -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(ItemAdminHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_ITEM -> ItemViewHolder(ItemAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val currentDisplayItem = displayItems[position]) {
            is AdminDisplayItem.Header -> {
                val headerHolder = holder as HeaderViewHolder
                headerHolder.binding.txtHeaderLevel.text = when (currentDisplayItem.level) {
                    1 -> "Nível Leve"
                    2 -> "Nível Moderado"
                    3 -> "Nível Extremo"
                    else -> "Nível Desconhecido"
                }
            }
            is AdminDisplayItem.Item -> {
                val itemHolder = holder as ItemViewHolder
                val originalItem = currentDisplayItem.textLevelItem

                // Mostra o texto principal
                itemHolder.binding.txtItem.text = originalItem.texto

                // *** MOSTRA O TEMPO SE FOR UM DESAFIO ***
                if (originalItem is DesafioEntity && originalItem.tempo > 0) {
                    val minutes = originalItem.tempo / 60
                    val seconds = originalItem.tempo % 60
                    val timeString = String.format("%02d:%02d", minutes, seconds)
                    itemHolder.binding.txtTempo.text = "Tempo: $timeString" // Assumindo que você tem um txtTempo no item_admin.xml
                    itemHolder.binding.txtTempo.visibility = View.VISIBLE
                } else {
                    itemHolder.binding.txtTempo.visibility = View.GONE // Esconde se não for desafio com tempo
                }
                // ***************************************

                itemHolder.binding.btnEdit.setOnClickListener { onEdit(originalItem) }
                itemHolder.binding.btnDelete.setOnClickListener { onDelete(originalItem) }
            }
        }
    }

    override fun getItemCount(): Int = displayItems.size

    // UpdateList agora recebe List<TextLevelItem>
    fun updateList(newOriginalItems: List<TextLevelItem>) {
        displayItems.clear()
        val grouped = newOriginalItems.groupBy { it.level }.toSortedMap()

        grouped.forEach { (level, itemsInLevel) ->
            displayItems.add(AdminDisplayItem.Header(level))
            itemsInLevel.forEach { originalItem ->
                displayItems.add(AdminDisplayItem.Item(originalItem))
            }
        }
        notifyDataSetChanged()
    }
}