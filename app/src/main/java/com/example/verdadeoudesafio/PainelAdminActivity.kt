package com.example.verdadeoudesafio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.verdadeoudesafio.admin.AdminPagerAdapter
import com.example.verdadeoudesafio.databinding.ActivityPainelAdminBinding
import com.google.android.material.tabs.TabLayoutMediator

class PainelAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPainelAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPainelAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = AdminPagerAdapter(this)
        binding.viewPager.adapter = adapter

        val titles = listOf("Perguntas", "Desafios", "Punições", "Raspadinhas")

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()

        supportActionBar?.title = "Painel Administrativo"
    }
}
