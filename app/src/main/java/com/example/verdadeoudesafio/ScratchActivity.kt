package com.example.verdadeoudesafio

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.data.entity.RaspadinhaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ScratchActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var gameManager: GameManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scratch_activity)

        val bgImage = findViewById<ImageView>(R.id.bgImage)
        val scratchView = findViewById<ScratchView>(R.id.scratchView)

        lifecycleScope.launch {
            db = AppDatabase.getDatabase(applicationContext)
            gameManager = GameManager(db)
            val raspadinha = getImagemAleatoriaSegura()

            withContext(Dispatchers.Main) {
                if (raspadinha != null) {
                    bgImage.setImageURI(Uri.fromFile(File(raspadinha.imagePath)))
                } else {
                    Toast.makeText(this@ScratchActivity, "Nenhuma raspadinha disponível.", Toast.LENGTH_SHORT).show()
                    finish() // ou continue com fundo vazio
                }
            }
        }

        scratchView.post {
            scratchView.setOverlayColor(Color.GRAY)
        }
    }

    /**
     * Busca uma raspadinha válida (arquivo existe) ou null.
     */
    private suspend fun getImagemAleatoriaSegura(): RaspadinhaEntity? {
        return withContext(Dispatchers.IO) {
            val raspadinha = gameManager.getRandomRaspadinha()
            if (raspadinha ==null) {
                Log.e("ScratchActivity", "Nenhuma raspadinha no banco.")
                return@withContext null
            }

            // Embaralha e procura a primeira com arquivo existente
            val file = File(raspadinha.imagePath)
            if (file.exists()) {
                raspadinha
            } else {
                    Log.w("ScratchActivity", "Arquivo não encontrado: ${raspadinha.imagePath}. Ignorando.")
                null
                }
            }
        }
    }
