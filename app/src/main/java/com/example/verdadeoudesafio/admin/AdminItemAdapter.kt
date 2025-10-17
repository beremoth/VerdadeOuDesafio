package com.example.verdadeoudesafio.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.verdadeoudesafio.databinding.ItemAdminBinding

class AdminItemAdapter(
    private var items: MutableList<String>,
    private val onEdit: (Int, String) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<AdminItemAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAdminBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.txtItem.text = item
        holder.binding.btnEdit.setOnClickListener { onEdit(position, item) }
        holder.binding.btnDelete.setOnClickListener { onDelete(position) }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
