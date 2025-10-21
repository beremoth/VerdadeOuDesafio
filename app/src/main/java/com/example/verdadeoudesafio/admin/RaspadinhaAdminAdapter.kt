package com.example.verdadeoudesafio.admin

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.verdadeoudesafio.data.entity.RaspadinhaEntity
import com.example.verdadeoudesafio.databinding.ItemAdminRaspadinhaBinding
import java.io.File

class RaspadinhaAdminAdapter(
    private var items: MutableList<RaspadinhaEntity>,
    private val context: Context,
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
        val binding = holder.binding

        val imagePath = item.imagePath
        var loaded = false

        // 1️⃣ Tenta carregar a imagem do caminho salvo no banco (armazenamento interno)
        if (!imagePath.isNullOrBlank()) {
            try {
                val file = File(imagePath)
                if (file.exists()) {
                    binding.imgPreview.setImageURI(Uri.fromFile(file))
                    loaded = true
                }
            } catch (_: Exception) { }
        }

        // 2️⃣ Se não encontrar no storage, tenta buscar nos assets/rapadinhas
        if (!loaded) {
            try {
                val assetManager = context.assets
                val assetList = assetManager.list("rapadinhas") ?: emptyArray()
                val fileName = imagePath.substringAfterLast('/')
                if (assetList.contains(fileName)) {
                    assetManager.open("rapadinhas/$fileName").use { inputStream ->
                        val drawable = android.graphics.drawable.Drawable.createFromStream(inputStream, null)
                        binding.imgPreview.setImageDrawable(drawable)
                        loaded = true
                    }
                }
            } catch (_: Exception) { }
        }

        // 3️⃣ Se ainda assim falhar, mostra imagem de erro
        if (!loaded) {
            binding.imgPreview.setImageResource(android.R.drawable.ic_delete)
        }

        // 4️⃣ Mostra apenas o nome da imagem
        binding.txtImagePath.text = imagePath.substringAfterLast('/', "Sem imagem")

        // 5️⃣ Ação de deletar
        binding.btnDeleteImage.setOnClickListener {
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
