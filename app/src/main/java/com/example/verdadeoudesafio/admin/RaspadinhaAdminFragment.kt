package com.example.verdadeoudesafio.admin

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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

    protected val db by lazy {
        AppDatabase.getDatabase(requireContext().applicationContext, lifecycleScope)
    }

    // --- Permissões e seletor de imagem ---
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) openGallery()
        else Log.w("RaspadinhaAdmin", "Permissão de leitura negada pelo usuário.")
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
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
        loadImages()
    }

    private fun setupRecyclerView() {
        adapter = RaspadinhaAdminAdapter(
            items = mutableListOf(),
            context = requireContext(),
            onDelete = { raspadinha -> showDeleteConfirmation(raspadinha)  }
        )

        binding.recyclerViewRaspadinhas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRaspadinhas.adapter = adapter
    }

    private fun setupAddButton() {
        binding.btnAddRaspadinha.setOnClickListener { checkAndRequestPermission() }
    }

    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openGallery()
            return
        }
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED -> openGallery()

            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) ->
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

            else -> permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun openGallery() {
        val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    /**
     * Carrega as imagens do banco e também as da pasta assets/rapadinhas
     */
    private fun loadImages() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dbImages = db.raspadinhaDao().getAll().toMutableList()

            // Adiciona também as imagens dos assets
            val assetImages = loadImagesFromAssets(requireContext())

            // Evita duplicatas (se o mesmo arquivo já foi salvo)
            val combinedList = mutableListOf<RaspadinhaEntity>()
            combinedList.addAll(dbImages)

            for (asset in assetImages) {
                val alreadyExists = dbImages.any { it.imagePath.endsWith(asset.imagePath.substringAfterLast("/")) }
                if (!alreadyExists) combinedList.add(asset)
            }

            withContext(Dispatchers.Main) {
                adapter.updateList(combinedList)
            }
        }
    }

    /**
     * Carrega imagens da pasta assets/rapadinhas
     */
    private fun loadImagesFromAssets(context: Context): List<RaspadinhaEntity> {
        val images = mutableListOf<RaspadinhaEntity>()
        try {
            val assetNames = context.assets.list("rapadinhas") ?: return emptyList()
            for (fileName in assetNames) {
                val tempFile = File(context.cacheDir, fileName)
                if (!tempFile.exists()) {
                    context.assets.open("rapadinhas/$fileName").use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                images.add(RaspadinhaEntity(imagePath = tempFile.absolutePath))
            }
        } catch (e: IOException) {
            Log.e("RaspadinhaAdmin", "Erro ao carregar assets: ${e.message}", e)
        }
        return images
    }

    private fun copyImageToInternalStorage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val fileName = getFileName(requireContext(), uri)
                ?: "raspadinha_${System.currentTimeMillis()}.jpg"

            val directory = File(requireContext().filesDir, "raspadinhas")
            if (!directory.exists()) directory.mkdirs()

            val destinationFile = File(directory, fileName)

            try {
                requireContext().contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }

                db.raspadinhaDao().insert(RaspadinhaEntity(imagePath = destinationFile.absolutePath))
                withContext(Dispatchers.Main) { loadImages() }

            } catch (e: IOException) {
                Log.e("RaspadinhaAdmin", "Erro ao copiar imagem: ${e.message}", e)
            }
        }
    }

    private fun showDeleteConfirmation(item: RaspadinhaEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja deletar esta imagem?")
            .setPositiveButton("Deletar") { _, _ -> deleteImage(item) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteImage(item: RaspadinhaEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val file = File(item.imagePath)
                if (file.exists()) file.delete()
                db.raspadinhaDao().delete(item)
                withContext(Dispatchers.Main) { loadImages() }
            } catch (e: Exception) {
                Log.e("RaspadinhaAdmin", "Erro ao deletar imagem: ${e.message}", e)
            }
        }
    }

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
