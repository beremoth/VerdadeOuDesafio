package com.example.verdadeoudesafio.admin

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.verdadeoudesafio.data.entity.RaspadinhaEntity
import com.example.verdadeoudesafio.databinding.ItemAdminRaspadinhaBinding
import java.io.File

class RaspadinhaAdminAdapter(
    private var items: MutableList<RaspadinhaEntity>,
    private val onDelete: (RaspadinhaEntity) -> Unit
) : RecyclerView.Adapter<RaspadinhaAdminAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAdminRaspadinhaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminRaspadinhaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        try {
            // Carrega a imagem a partir do caminho salvo no DB
            val file = File(item.imagePath)
            holder.binding.imgRaspadinha.setImageURI(Uri.fromFile(file))
        } catch (e: Exception) {
            // Se der erro, mostra um ícone padrão (opcional)
            // holder.binding.imgRaspadinha.setImageResource(R.drawable.ic_error)
        }

        holder.binding.btnDeleteRaspadinha.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<RaspadinhaEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}