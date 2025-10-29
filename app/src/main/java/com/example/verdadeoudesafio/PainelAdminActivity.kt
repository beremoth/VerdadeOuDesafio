package com.example.verdadeoudesafio

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.verdadeoudesafio.admin.AdminPagerAdapter
import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.data.database.DatabaseInitializer
import com.example.verdadeoudesafio.databinding.ActivityPainelAdminBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PainelAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPainelAdminBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPainelAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        val adapter = AdminPagerAdapter(this)
        binding.viewPager.adapter = adapter

        val titles = listOf("Perguntas", "Desafios", "Punições", "Raspadinhas")

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]

        }.attach()

        binding.btnRestoreFromJson.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Restaurar Conteúdo")
                .setMessage("Isso apagará todas as alterações feitas e restaurará o conteúdo original dos assets. Continuar?")
                .setPositiveButton("Restaurar") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val initializer = DatabaseInitializer(this@PainelAdminActivity)
                        initializer.reloadAllContentFromAssets(db, 0) // Força recarga
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@PainelAdminActivity, "Conteúdo restaurado!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}
