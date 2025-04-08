package com.example.verdadeoudesafio

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.Button
import com.example.verdadeoudesafio.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Infla o layout activity_menu.xml
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura os listeners
        binding.startButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.instructionsButton.setOnClickListener {
            // Cria um AlertDialog personalizado
            val alertDialogBuilder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.custom_dialog, null)

            // Define o layout personalizado
            alertDialogBuilder.setView(dialogView)

            // Vincula os componentes do layout personalizado
            val dialogButton = dialogView.findViewById<Button>(R.id.dialogButton)

            // Configura o botão "Entendido"
            dialogButton.setOnClickListener {
                // Fecha o diálogo quando o botão for clicado
                alertDialogBuilder.create().dismiss()
            }

            // Exibe o popup
            alertDialogBuilder.show()
        }

        binding.createQuestionsButton.setOnClickListener {
            val intent = Intent(this, CreateQuestionsActivity::class.java)
            startActivity(intent)
        }

        binding.settingsButton.setOnClickListener {
            startActivity(SettingsActivity.getIntent(this))
        }
    }
}