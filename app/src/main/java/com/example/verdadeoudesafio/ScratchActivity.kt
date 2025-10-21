package com.example.verdadeoudesafio

import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
// import androidx.room.Room // Removido
import com.example.verdadeoudesafio.data.database.AppDatabase // Importado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import android.content.res.AssetManager

sealed class ImageSource {
    data class Asset(val fileName: String) : ImageSource()
    data class Path(val filePath: String) : ImageSource()
}

class ScratchActivity : AppCompatActivity() {

    private val assetImageFiles: List<String> by lazy {
        findImageFilesInAssets(assets, "raspadinhas")
    }

    private val db by lazy {
        AppDatabase.getDatabase(applicationContext, lifecycleScope)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scratch_activity)

        val bgImage = findViewById<ImageView>(R.id.bgImage)
        val scratchView = findViewById<ScratchView>(R.id.scratchView)

        lifecycleScope.launch {
            val imageSource = getImagemAleatoria()

            if (imageSource == null) {
                Log.e("ScratchActivity", "Nenhuma imagem encontrada (nem no assets, nem no DB).")
                return@launch
            }

            try {
                when (imageSource) {
                    // Carrega da pasta ASSETS/RASPADINHAS
                    is ImageSource.Asset -> {
                        Log.d("ScratchActivity", "Carregando do Assets: ${imageSource.fileName}")
                        // Precisa adicionar o caminho da pasta
                        val inputStream = assets.open("raspadinhas/${imageSource.fileName}")
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        bgImage.setImageBitmap(bitmap)
                        inputStream.close()
                    }

                    // Carrega do armazenamento interno (imagens salvas pelo Admin)
                    is ImageSource.Path -> {
                        Log.d("ScratchActivity", "Carregando do DB/Path: ${imageSource.filePath}")
                        val file = File(imageSource.filePath)
                        val uri = Uri.fromFile(file)
                        bgImage.setImageURI(uri)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("ScratchActivity", "Erro ao carregar a imagem: ${e.message}")
            }
        }

        scratchView.post {
            scratchView.setOverlayColor(Color.GRAY)
        }
    }

    private suspend fun getImagemAleatoria(): ImageSource? {
        return withContext(Dispatchers.IO) {
            val assetList = assetImageFiles.map { ImageSource.Asset(it) }
            val dbList = db.raspadinhaDao().getAll().map { ImageSource.Path(it.imagePath) }
            val fullList = assetList + dbList
            fullList.randomOrNull()
        }
    }

    // Esta função agora lê da subpasta 'raspadinhas'
    private fun findImageFilesInAssets(assetManager: AssetManager, path: String): List<String> {
        return try {
            assetManager.list(path)
                ?.filterNotNull()
                ?.filter { fileName ->
                    fileName.endsWith(".jpg", ignoreCase = true) ||
                            fileName.endsWith(".jpeg", ignoreCase = true) ||
                            fileName.endsWith(".png", ignoreCase = true) ||
                            fileName.endsWith(".webp", ignoreCase = true)
                }
                ?: emptyList()
        } catch (e: IOException) {
            Log.e("ScratchActivity", "Erro ao listar arquivos em assets/$path", e)
            emptyList()
        }
    }
}