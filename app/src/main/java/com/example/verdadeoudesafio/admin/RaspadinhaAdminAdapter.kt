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
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Carrega a imagem do arquivo salvo no armazenamento interno
        try {
            val file = File(item.imagePath)
            if (file.exists()) {
                holder.binding.imgPreview.setImageURI(Uri.fromFile(file))
            } else {
                holder.binding.imgPreview.setImageResource(android.R.drawable.ic_dialog_alert) // Imagem de erro
            }
        } catch (e: Exception) {
            holder.binding.imgPreview.setImageResource(android.R.drawable.ic_dialog_alert)
        }

        holder.binding.txtImagePath.text = item.imagePath.substringAfterLast('/') // Mostra só o nome do arquivo
        holder.binding.btnDeleteImage.setOnClickListener {
            onDelete(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<RaspadinhaEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}