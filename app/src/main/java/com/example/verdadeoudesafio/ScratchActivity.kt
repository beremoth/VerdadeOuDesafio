package com.example.verdadeoudesafio

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class ScratchActivity : AppCompatActivity() {

    // Lista de imagens que serão escolhidas aleatoriamente
    private val imagens = listOf(
        R.raw.imagens.imagem1,
        R.raw.imagens.imagem2,
        R.raw.imagens.imagem3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scratch_activity)

        val bgImage = findViewById<ImageView>(R.id.bgImage)
        val scratchView = findViewById<ScratchView>(R.id.scratchView)

        // Escolhe uma imagem aleatória
        val randomImage = imagens.random()
        bgImage.setImageResource(randomImage)

        scratchView.post {
            scratchView.setOverlayColor(Color.GRAY) // cor da raspadinha
        }
    }
}
