package com.example.verdadeoudesafio.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.verdadeoudesafio.databinding.ItemAdminBinding
import com.example.verdadeoudesafio.databinding.ItemAdminHeaderBinding

class AdminItemAdapter(
    private val onEdit: (TextLevelItem) -> Unit,
    private val onDelete: (TextLevelItem) -> Unit
) : ListAdapter<AdminListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AdminListItem.Header -> TYPE_HEADER
            is AdminListItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemAdminHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            TYPE_ITEM -> {
                val binding = ItemAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Tipo de view inválido")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = getItem(position) as AdminListItem.Header
                holder.bind(header.level)
            }
            is ItemViewHolder -> {
                val item = getItem(position) as AdminListItem.Item
                holder.bind(item.item, onEdit, onDelete)
            }
        }
    }

    class HeaderViewHolder(private val binding: ItemAdminHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(level: Int) {
            binding.txtHeaderLevel.text = when (level) {
                1 -> "Nível: Leve"
                2 -> "Nível: Moderado"
                3 -> "Nível: Extremo"
                else -> "Nível: $level"
            }
        }
    }

    class ItemViewHolder(private val binding: ItemAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: TextLevelItem,
            onEdit: (TextLevelItem) -> Unit,
            onDelete: (TextLevelItem) -> Unit
        ) {
            binding.txtItem.text = item.texto

            val tempo = item.tempo ?: 0
            if (tempo > 0) {
                binding.txtTempo.visibility = View.VISIBLE
                binding.txtTempo.text = "${tempo}s"
            } else {
                binding.txtTempo.visibility = View.GONE
            }

            binding.btnEdit.setOnClickListener { onEdit(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AdminListItem>() {
        override fun areItemsTheSame(oldItem: AdminListItem, newItem: AdminListItem): Boolean {
            return when {
                oldItem is AdminListItem.Header && newItem is AdminListItem.Header ->
                    oldItem.level == newItem.level
                oldItem is AdminListItem.Item && newItem is AdminListItem.Item ->
                    oldItem.item.id == newItem.item.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: AdminListItem, newItem: AdminListItem): Boolean {
            return oldItem == newItem
        }
    }
}