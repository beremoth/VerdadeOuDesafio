package com.example.verdadeoudesafio

import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.verdadeoudesafio.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import android.content.res.AssetManager

/**
 * Classe auxiliar (está correta, não mude)
 */
sealed class ImageSource {
    data class Asset(val fileName: String) : ImageSource()
    data class Path(val filePath: String) : ImageSource() // Mudei o nome para 'filePath' para ficar mais claro
}

class ScratchActivity : AppCompatActivity() {

    // Lista de imagens padrão (está correta)
    private val assetImageFiles: List<String> by lazy {
        findImageFilesInAssets(assets, "")
    }

    // Instância do DB (está correta)
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "verdade_ou_desafio_db" // Use o nome exato do seu DB
        ).build()
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
                    // Se for do ASSETS, usamos a lógica de assets (está correto)
                    is ImageSource.Asset -> {
                        Log.d("ScratchActivity", "Carregando do Assets: ${imageSource.fileName}")
                        val inputStream = assets.open(imageSource.fileName)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        bgImage.setImageBitmap(bitmap)
                        inputStream.close()
                    }

                    // ### A MUDANÇA ESTÁ AQUI ###
                    // Se for do BANCO (agora um caminho de arquivo), usamos a lógica de File
                    is ImageSource.Path -> {
                        Log.d("ScratchActivity", "Carregando do DB/Path: ${imageSource.filePath}")

                        // 1. Cria um objeto File a partir do caminho salvo no DB
                        val file = File(imageSource.filePath)

                        // 2. Cria um URI a partir do File
                        val uri = Uri.fromFile(file)

                        // 3. Define o URI no ImageView
                        bgImage.setImageURI(uri)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("ScratchActivity", "Erro ao carregar a imagem: ${e.message}")
            }
        }

        // Configura a raspadinha
        scratchView.post {
            scratchView.setOverlayColor(Color.GRAY)
        }
    }

    /**
     * Esta função junta as imagens e sorteia uma.
     * (Corrigi para usar o nome 'filePath' da classe selada)
     */
    private suspend fun getImagemAleatoria(): ImageSource? {
        return withContext(Dispatchers.IO) {
            // 1. Pega as imagens padrão (da pasta assets)
            val assetList = assetImageFiles.map { ImageSource.Asset(it) }

            // 2. Pega as imagens customizadas (do banco de dados)
            val dbList = db.raspadinhaDao().getAll().map { ImageSource.Path(it.imagePath) }

            // 3. Junta as duas listas
            val fullList = assetList + dbList

            // 4. Sorteia uma imagem da lista completa
            fullList.randomOrNull()
        }
    }
    private fun findImageFilesInAssets(assetManager: AssetManager, path: String): List<String> {
        return try {
            // Lista todos os arquivos e pastas no caminho especificado
            assetManager.list(path)
                ?.filterNotNull() // Garante que não há nulos
                ?.filter { fileName ->
                    // Filtra apenas arquivos que terminam com extensões de imagem comuns (case-insensitive)
                    fileName.endsWith(".jpg", ignoreCase = true) ||
                            fileName.endsWith(".jpeg", ignoreCase = true) ||
                            fileName.endsWith(".png", ignoreCase = true) ||
                            fileName.endsWith(".webp", ignoreCase = true)
                }
                ?: emptyList() // Retorna lista vazia se list() retornar null
        } catch (e: IOException) {
            Log.e("ScratchActivity", "Erro ao listar arquivos em assets/$path", e)
            emptyList() // Retorna lista vazia em caso de erro
        }
    }
}