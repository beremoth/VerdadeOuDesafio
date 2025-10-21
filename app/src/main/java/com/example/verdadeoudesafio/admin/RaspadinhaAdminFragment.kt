package com.example.verdadeoudesafio.admin

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

    private lateinit var raspadinhaAdapter: RaspadinhaAdminAdapter

    private val db by lazy {
        Room.databaseBuilder(requireContext(), AppDatabase::class.java, "verdade_ou_desafio_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    // 1. Este é o 'lançador' que abre a galeria e espera um resultado
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent() // Abre a galeria
    ) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult // Usuário cancelou

        viewLifecycleOwner.lifecycleScope.launch {
            // 2. Chama a função para copiar a imagem
            val newPath = saveImageToInternalStorage(uri)

            if (newPath != null) {
                // 3. Salva o NOVO caminho (para a cópia) no banco
                db.raspadinhaDao().insert(
                    RaspadinhaEntity(imagePath = newPath)
                )
                Toast.makeText(requireContext(), "Imagem salva!", Toast.LENGTH_SHORT).show()
                refreshList() // Atualiza a lista na tela
            } else {
                Toast.makeText(requireContext(), "Falha ao salvar imagem.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminRaspadinhaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        setupButtons()
        refreshList()
    }

    private fun setupRecycler() {
        raspadinhaAdapter = RaspadinhaAdminAdapter(
            mutableListOf(),
            onDelete = { entity ->
                // Lógica para deletar a imagem
                lifecycleScope.launch(Dispatchers.IO) {
                    db.raspadinhaDao().delete(entity)
                    // Deleta o arquivo físico do armazenamento
                    try {
                        File(entity.imagePath).delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    // Atualiza a UI na thread principal
                    withContext(Dispatchers.Main) {
                        refreshList()
                    }
                }
            }
        )
        binding.recyclerViewRaspadinhas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRaspadinhas.adapter = raspadinhaAdapter
    }

    private fun setupButtons() {
        binding.btnAddImage.setOnClickListener {
            // Inicia o processo: abre a galeria
            galleryLauncher.launch("image/*")
        }
    }

    private fun refreshList() {
        lifecycleScope.launch(Dispatchers.IO) {
            val items = db.raspadinhaDao().getAll()
            withContext(Dispatchers.Main) {
                raspadinhaAdapter.updateList(items)
            }
        }
    }

    /**
     * Copia a imagem da galeria (URI) para o armazenamento interno do app
     * e retorna o caminho absoluto do novo arquivo.
     */
    private suspend fun saveImageToInternalStorage(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val fileName = "raspadinha_${System.currentTimeMillis()}.jpg"
                val file = File(requireContext().filesDir, fileName) // Salva na pasta 'files' privada
                val outputStream = FileOutputStream(file)

                inputStream?.copyTo(outputStream)

                inputStream?.close()
                outputStream.close()

                // Retorna o caminho para o NOVO arquivo
                file.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
                null // Retorna nulo se der erro
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}