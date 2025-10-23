package com.example.verdadeoudesafio

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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
    private lateinit var bgImage: ImageView
    private lateinit var scratchView: ScratchView
    private lateinit var btnSortearOutra: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scratch_activity)

        bgImage = findViewById(R.id.bgImage)
        scratchView = findViewById(R.id.scratchView)
        btnSortearOutra = findViewById(R.id.btnSortearOutra)

        lifecycleScope.launch {
            db = AppDatabase.getDatabase(applicationContext)
            gameManager = GameManager(db)
            carregarRaspadinha()
        }

        // Configura o botão de "Sortear outra"
        btnSortearOutra.setOnClickListener {
            btnSortearOutra.visibility = View.GONE
            scratchView.reset()
            lifecycleScope.launch {
                carregarRaspadinha()
            }
        }

        // Verifica a porcentagem raspada a cada toque
        scratchView.setOnScratchListener { percentage ->
            if (percentage > 40f) {
                btnSortearOutra.visibility = View.VISIBLE
            }
        }
    }

    private suspend fun carregarRaspadinha() {
        val raspadinha = getImagemAleatoriaSegura()
        withContext(Dispatchers.Main) {
            if (raspadinha != null) {
                bgImage.setImageURI(Uri.fromFile(File(raspadinha.imagePath)))
            } else {
                Toast.makeText(this@ScratchActivity, "Nenhuma raspadinha disponível.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private suspend fun getImagemAleatoriaSegura(): RaspadinhaEntity? {
        return withContext(Dispatchers.IO) {
            val raspadinha = gameManager.getRandomRaspadinha()
            if (raspadinha == null) {
                Log.e("ScratchActivity", "Nenhuma raspadinha no banco.")
                return@withContext null
            }

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