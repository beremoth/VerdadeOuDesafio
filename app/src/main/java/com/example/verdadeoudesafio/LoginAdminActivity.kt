package com.example.verdadeoudesafio

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.verdadeoudesafio.databinding.ActivityLoginAdminBinding

class LoginAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginAdminBinding
    private lateinit var prefs: SharedPreferences
    private val defaultPassword = "1234"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Acesso Administrativo"

        prefs = getSharedPreferences("config_admin", MODE_PRIVATE)
        val senhaSalva = prefs.getString("senha_admin", defaultPassword)

        binding.btnEntrar.setOnClickListener {
            val senhaDigitada = binding.etSenha.text.toString().trim()

            if (senhaDigitada == senhaSalva) {
                Toast.makeText(this, "Acesso liberado!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, PainelAdminActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }
}
