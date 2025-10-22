package com.example.verdadeoudesafio


import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.data.entity.RaspadinhaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException


class ScratchActivity : AppCompatActivity() {


    private lateinit var db: AppDatabase
    private lateinit var gameManager: GameManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scratch_activity)

        lifecycleScope.launch(Dispatchers.IO) {
            db = AppDatabase.getDatabase(applicationContext, this)
            gameManager = GameManager(db)
        }


        val bgImage = findViewById<ImageView>(R.id.bgImage)
        val scratchView = findViewById<ScratchView>(R.id.scratchView)

        lifecycleScope.launch {
            // 1. Busca UMA imagem aleatória (do banco) através do GameManager
            val raspadinha = getImagemAleatoria() // Agora retorna RaspadinhaEntity?

            if (raspadinha == null) {
                Log.e("ScratchActivity", "Nenhuma imagem encontrada no banco de dados.")
                return@launch
            }

            try {
                // 2. Não existe mais "when", pois SÓ temos um tipo (Path)
                Log.d("ScratchActivity", "Carregando do DB/Path: ${raspadinha.imagePath}")
                val file = File(raspadinha.imagePath)
                val uri = Uri.fromFile(file)
                bgImage.setImageURI(uri)

            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("ScratchActivity", "Erro ao carregar a imagem: ${e.message}")
            }
        }

        scratchView.post {
            scratchView.setOverlayColor(Color.GRAY)
        }
    }

    /**
     * Esta função agora pede ao GameManager uma imagem aleatória do banco.
     */
    private suspend fun getImagemAleatoria(): RaspadinhaEntity? {
        return withContext(Dispatchers.IO) {
            gameManager.getRandomRaspadinha()
        }
    }
}