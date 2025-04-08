package com.example.verdadeoudesafio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import android.content.SharedPreferences
import android.util.Log


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Vincula os componentes da interface
        val easyLevel = findViewById<RadioButton>(R.id.easyLevel)
        val mediumLevel = findViewById<RadioButton>(R.id.mediumLevel)
        val hardLevel = findViewById<RadioButton>(R.id.hardLevel)
        val saveButton = findViewById<Button>(R.id.saveButton)

        // Carrega o nível salvo anteriormente
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val savedLevel = sharedPreferences.getInt("selected_level", 2) // Padrão: Moderado (2)
        when (savedLevel) {
            1 -> easyLevel.isChecked = true
            2 -> mediumLevel.isChecked = true
            3 -> hardLevel.isChecked = true
        }

        // Configura o listener do botão Salvar
        saveButton.setOnClickListener {
            val selectedLevel = when {
                easyLevel.isChecked -> 1
                mediumLevel.isChecked -> 2
                hardLevel.isChecked -> 3
                else -> 2 // Padrão: Moderado
            }

            // Salva o nível selecionado no SharedPreferences
            sharedPreferences.edit().putInt("selected_level", selectedLevel).apply()
            Log.d("SettingsActivity", "Nível salvo: $selectedLevel")


            // Exibe uma mensagem de confirmação
            Toast.makeText(this, "Nível salvo com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }

}
