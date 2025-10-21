package com.example.verdadeoudesafio.admin

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.data.entity.RaspadinhaEntity
import com.example.verdadeoudesafio.databinding.FragmentAdminRaspadinhaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RaspadinhaAdminFragment : Fragment() {

    private var _binding: FragmentAdminRaspadinhaBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RaspadinhaAdminAdapter
    private val db by lazy {
        Room.databaseBuilder(
            requireContext().applicationContext,
            AppDatabase::class.java,
            "verdade_ou_desafio_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // Prepara o "lançador" para pegar a imagem da galeria
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Imagem selecionada, agora vamos copiá-la
                copyImageToInternalStorage(uri)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminRaspadinhaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupAddButton()
        loadImagesFromDb()
    }

    private fun setupRecyclerView() {
        adapter = RaspadinhaAdminAdapter(
            mutableListOf(),
            onDelete = { item ->
                showDeleteConfirmation(item)
            }
        )
        binding.recyclerViewRaspadinhas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRaspadinhas.adapter = adapter
    }

    private fun setupAddButton() {
        binding.btnAddRaspadinha.setOnClickListener {
            // Cria uma Intent para abrir a galeria
            val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT)
            intent.type = "image/*" // Permite selecionar qualquer tipo de imagem
            imagePickerLauncher.launch(intent) // Lança o seletor de imagem
        }
    }

    private fun loadImagesFromDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            val imageList = db.raspadinhaDao().getAll()
            withContext(Dispatchers.Main) {
                adapter.updateList(imageList)
            }
        }
    }

    private fun copyImageToInternalStorage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val fileName = getFileName(requireContext(), uri) ?: "raspadinha_${System.currentTimeMillis()}.jpg"
            val directory = File(requireContext().filesDir, "raspadinhas")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val destinationFile = File(directory, fileName)

            try {
                // Copia o stream da imagem (da galeria) para o novo arquivo (no app)
                requireContext().contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }

                // Se a cópia deu certo, salva o CAMINHO no banco de dados
                val newRaspadinha = RaspadinhaEntity(imagePath = destinationFile.absolutePath)
                db.raspadinhaDao().insert(newRaspadinha)

                // Recarrega a lista na thread principal
                withContext(Dispatchers.Main) {
                    loadImagesFromDb()
                }

            } catch (e: IOException) {
                Log.e("RaspadinhaAdmin", "Erro ao copiar imagem: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    // Mostra erro
                }
            }
        }
    }

    private fun showDeleteConfirmation(item: RaspadinhaEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja deletar esta imagem?")
            .setPositiveButton("Deletar") { _, _ ->
                deleteImage(item)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteImage(item: RaspadinhaEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Deleta o arquivo físico do armazenamento interno
                val file = File(item.imagePath)
                if (file.exists()) {
                    file.delete()
                }

                // 2. Deleta a referência do banco de dados
                db.raspadinhaDao().delete(item)

                // 3. Recarrega a lista
                withContext(Dispatchers.Main) {
                    loadImagesFromDb()
                }
            } catch (e: Exception) {
                Log.e("RaspadinhaAdmin", "Erro ao deletar imagem: ${e.message}", e)
            }
        }
    }

    // Função auxiliar para tentar pegar o nome original do arquivo
    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        return it.getString(nameIndex)
                    }
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}