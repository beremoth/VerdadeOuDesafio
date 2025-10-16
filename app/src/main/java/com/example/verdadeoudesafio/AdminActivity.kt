package com.example.verdadeoudesafio

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.verdadeoudesafio.databinding.ActivityAdminBinding
import android.widget.Toast
import android.widget.EditText
import android.widget.Button

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verifica senha simples (pode ser melhorada depois)
        val btnEntrar = findViewById<Button>(R.id.btnEntrarAdmin)
        val senhaInput = findViewById<EditText>(R.id.senhaAdminInput)

        btnEntrar.setOnClickListener {
            if (senhaInput.text.toString() == "admin123") {
                Toast.makeText(this, "Acesso liberado!", Toast.LENGTH_SHORT).show()
                // Aqui você pode abrir o painel real de edição
                startActivity(Intent(this, PainelAdminActivity::class.java))
            } else {
                Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
