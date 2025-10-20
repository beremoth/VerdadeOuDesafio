package com.example.verdadeoudesafio

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.verdadeoudesafio.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Infla o layout activity_menu.xml
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // botão para verdade ou desafio
        binding.startButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
                // botão para rapadinha
        binding.btnRaspadinha.setOnClickListener {
            val intent = Intent(this, ScratchActivity::class.java)
            startActivity(intent)
        }


        binding.instructionsButton.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.custom_dialog, null)

            alertDialogBuilder.setView(dialogView)

            // Criando o AlertDialog antes de configurar o botão
            val alertDialog = alertDialogBuilder.create()

            // Vincula os componentes do layout personalizado
            val dialogButton = dialogView.findViewById<Button>(R.id.dialogButton)

            // Configura o botão "Entendido"
            dialogButton.setOnClickListener {
                alertDialog.dismiss() // Fecha o diálogo corretamente
            }

            // Exibe o popup
            alertDialog.show()
        }

        binding.settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}