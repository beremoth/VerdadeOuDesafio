package com.example.verdadeoudesafio

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class SettingsActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var saveButton: Button
    private lateinit var btnAdmin: Button
    private lateinit var playersContainer: LinearLayout
    private lateinit var addPlayerButton: Button
    private lateinit var playerNameInput: EditText
    private val playerFields = mutableListOf<EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        val easyLevel = findViewById<RadioButton>(R.id.easyLevel)
        val mediumLevel = findViewById<RadioButton>(R.id.mediumLevel)
        val hardLevel = findViewById<RadioButton>(R.id.hardLevel)
        playersContainer = findViewById(R.id.playersContainer)
        addPlayerButton = findViewById(R.id.addPlayerButton)
        playerNameInput = findViewById(R.id.playerNameInput)
        saveButton = findViewById(R.id.saveButton)
        btnAdmin = findViewById(R.id.btn_admin)


        // Carrega o nível salvo
        val savedLevel = sharedPreferences.getInt("selected_level", 2)
        when (savedLevel) {
            1 -> easyLevel.isChecked = true
            2 -> mediumLevel.isChecked = true
            3 -> hardLevel.isChecked = true
        }

        // Carrega os jogadores salvos
        loadPlayers()

        // Adiciona um novo campo de jogador ao clicar no botão "+"
        addPlayerButton.setOnClickListener {
            val playerName = playerNameInput.text.toString().trim()
            if (playerName.isNotEmpty()) {
                addPlayerField(playerName)
                playerNameInput.text.clear()
            } else {
                Toast.makeText(this, "Digite um nome antes de adicionar!", Toast.LENGTH_SHORT).show()
            }
        }


        btnAdmin.setOnClickListener {
            openAdmin(View(this))
        }

        // Salva as configurações ao clicar no botão "Salvar"
        saveButton.setOnClickListener {
            saveSettings()
        }
    }

    fun openAdmin(view: View) {
        startActivity(Intent(this, LoginAdminActivity::class.java))
    }

    private fun loadPlayers() {
        val savedPlayers = sharedPreferences.getStringSet("players_list", emptySet()) ?: emptySet()
        if (savedPlayers.isNotEmpty()) {
            savedPlayers.forEach { addPlayerField(it) }
        }
    }

    private fun addPlayerField(playerName: String) {
        val container = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 10, 0, 10)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        val newPlayerInput = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            hint = "Nome do jogador"
            setPadding(12, 12, 12, 12)
            setBackgroundResource(R.drawable.background_gradient)
            setTextColor(resources.getColor(R.color.white, theme))
            setHintTextColor(resources.getColor(R.color.white, theme))
            setText(playerName)
        }

        val removeButton = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(10, 0, 0, 0)
            }
            text = "-"
            textSize = 18f
            setBackgroundColor(resources.getColor(R.color.purple_500, theme))
            setTextColor(resources.getColor(R.color.white, theme))
            setOnClickListener {
                playersContainer.removeView(container)
                playerFields.remove(newPlayerInput)
            }
        }

        container.addView(newPlayerInput)
        container.addView(removeButton)

        playerFields.add(newPlayerInput)
        playersContainer.addView(container)
    }

    private fun saveSettings() {
        val selectedLevel = when {
            findViewById<RadioButton>(R.id.easyLevel).isChecked -> 1
            findViewById<RadioButton>(R.id.mediumLevel).isChecked -> 2
            findViewById<RadioButton>(R.id.hardLevel).isChecked -> 3
            else -> 2
        }

        val playerNames = playerFields.mapNotNull { it.text.toString().trim() }.filter { it.isNotEmpty() }

        if (playerNames.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos um jogador!", Toast.LENGTH_SHORT).show()
            return
        }

        sharedPreferences.edit {
            putInt("selected_level", selectedLevel)
            putStringSet("players_list", playerNames.toSet())
        }

        Log.d("SettingsActivity", "Nível salvo: $selectedLevel")
        Log.d("SettingsActivity", "Jogadores salvos: $playerNames")

        Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
