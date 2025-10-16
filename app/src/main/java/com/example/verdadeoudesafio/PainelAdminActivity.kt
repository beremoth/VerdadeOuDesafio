package com.example.verdadeoudesafio // ajuste para o seu pacote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.verdadeoudesafio.databinding.ActivityPainelAdminBinding


class PainelAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPainelAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPainelAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuração do título
        supportActionBar?.title = "Painel Administrativo"

        // Exemplo de funcionalidade
        binding.btnResetarDados.setOnClickListener {
            Toast.makeText(this, "Função de reset ainda não implementada!", Toast.LENGTH_SHORT).show()
        }

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }
}
