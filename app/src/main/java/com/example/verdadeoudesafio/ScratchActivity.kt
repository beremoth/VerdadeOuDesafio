package com.example.verdadeoudesafio

import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri // Importação importante
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
import java.io.IOException

/**
 * 1. CLASSE AUXILIAR
 * Define a origem da imagem: da pasta 'assets' (padrão) ou do Banco de Dados (admin).
 * (Pode colocar esta classe no mesmo arquivo, mas fora da classe ScratchActivity)
 */
sealed class ImageSource {
    data class Asset(val fileName: String) : ImageSource() // Imagem da pasta assets
    data class Path(val uriString: String) : ImageSource()  // Imagem do banco (galeria)
}

class ScratchActivity : AppCompatActivity() {

    // 2. LISTA DE IMAGENS PADRÃO
    // Lista dos NOMES dos arquivos que estão na sua pasta 'assets'
    private val defaultAssetImages = listOf(
        "imagem1.jpg",
        "imagem2.jpg",
        "imagem3.jpg"
    )

    // Instância do Banco de Dados
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

        // 3. INICIA A LÓGICA DE CARREGAMENTO
        lifecycleScope.launch {
            // Busca uma fonte de imagem aleatória (do assets OU do banco)
            val imageSource = getImagemAleatoria()

            if (imageSource == null) {
                Log.e("ScratchActivity", "Nenhuma imagem encontrada (nem no assets, nem no DB).")
                // TODO: Colocar uma imagem de erro padrão aqui
                return@launch
            }

            // 4. CARREGA A IMAGEM (A PARTE IMPORTANTE)
            // Agora verificamos a ORIGEM da imagem para saber como carregá-la
            try {
                when (imageSource) {
                    // Se for do ASSETS, usamos a lógica de assets
                    is ImageSource.Asset -> {
                        Log.d("ScratchActivity", "Carregando do Assets: ${imageSource.fileName}")
                        val inputStream = assets.open(imageSource.fileName)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        bgImage.setImageBitmap(bitmap)
                        inputStream.close()
                    }
                    // Se for do BANCO, usamos a lógica de URI (caminho)
                    is ImageSource.Path -> {
                        Log.d("ScratchActivity", "Carregando do DB/Path: ${imageSource.uriString}")
                        bgImage.setImageURI(Uri.parse(imageSource.uriString))
                    }
                }
            } catch (e: IOException) { // Trata erros como FileNotFoundException
                e.printStackTrace()
                Log.e("ScratchActivity", "Erro ao carregar a imagem: ${e.message}")
                // TODO: Colocar imagem de erro padrão
            }
        }

        // Configura a raspadinha (isso permanece igual)
        scratchView.post {
            scratchView.setOverlayColor(Color.GRAY)
        }
    }

    /**
     * Esta função junta as imagens do Assets e do Banco de Dados e sorteia uma.
     */
    private suspend fun getImagemAleatoria(): ImageSource? {
        return withContext(Dispatchers.IO) {
            // 1. Pega as imagens padrão (da pasta assets)
            val assetList = defaultAssetImages.map { ImageSource.Asset(it) }

            // 2. Pega as imagens customizadas (do banco de dados)
            val dbList = db.raspadinhaDao().getAll().map { ImageSource.Path(it.imagePath) }

            // 3. Junta as duas listas
            val fullList = assetList + dbList

            // 4. Sorteia uma imagem da lista completa
            fullList.randomOrNull()
        }
    }
}