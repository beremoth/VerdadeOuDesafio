package com.example.verdadeoudesafio.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.verdadeoudesafio.databinding.ItemAdminBinding

class AdminItemAdapter<T : TextLevelItem>(
    private val onEdit: (T) -> Unit,
    private val onDelete: (T) -> Unit
) : ListAdapter<T, AdminItemAdapter<T>.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(val binding: ItemAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: T) {
            binding.txtItemText.text = item.texto // MUDADO PARA 'texto'
            binding.txtItemLevel.text = "Nível ${item.level}"

            if (item.tempo != null && item.tempo!! > 0) { // MUDADO PARA 'tempo'
                binding.txtItemTime.visibility = View.VISIBLE
                binding.txtItemTime.text = "${item.tempo}s" // MUDADO PARA 'tempo'
            } else {
                binding.txtItemTime.visibility = View.GONE
            }

            binding.btnEdit.setOnClickListener { onEdit(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback<T : TextLevelItem> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }
    }
}