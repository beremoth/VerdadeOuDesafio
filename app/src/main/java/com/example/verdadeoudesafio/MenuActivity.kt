package com.example.verdadeoudesafio

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.verdadeoudesafio.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMenuCards()
    }

    private fun setupMenuCards() {
        // --- Verdade ou Desafio ---
        configureCard(
            cardId = R.id.card_truth_dare,
            iconRes = R.drawable.ic_flame,
            title = "Verdade ou Desafio"
        ) {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // --- Raspadinha ---
        configureCard(
            cardId = R.id.card_raspadinha,
            iconRes = R.drawable.ic_image,
            title = "Raspadinha"
        ) {
            startActivity(Intent(this, ScratchActivity::class.java))
        }

        // --- Configurações ---
        configureCard(
            cardId = R.id.card_configuracoes,
            iconRes = R.drawable.ic_gear,
            title = "Configurações"
        ) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        configureCard(
            cardId = R.id.card_informacoes,
            iconRes = R.drawable.ic_info,
            title = "Informações"

        ){
            showInstructionsDialog()
        }
    }

    private fun configureCard(cardId: Int, iconRes: Int, title: String, onClick: () -> Unit) {
        val card = findViewById<View>(cardId)
        val icon = card.findViewById<ImageView>(R.id.icon)
        val titleView = card.findViewById<TextView>(R.id.title)

        icon.setImageResource(iconRes)
        titleView.text = title

        // Animação ao toque
        card.setOnClickListener {
            card.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                card.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                onClick()
            }.start()
        }
    }

    private fun showInstructionsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog, null)
        val dialogButton = dialogView.findViewById<Button>(R.id.dialogButton)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogButton.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }
}
