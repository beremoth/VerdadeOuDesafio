package com.example.verdadeoudesafio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.verdadeoudesafio.admin.RaspadinhaAdminFragment
import com.example.verdadeoudesafio.databinding.ActivityPainelAdminBinding
import com.google.android.material.tabs.TabLayout

class PainelAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPainelAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPainelAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Painel Administrativo"

        // Tabs
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Raspadinhas"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Perguntas"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Desafios"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Punições"))

        replaceFragment(RaspadinhaAdminFragment())

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val fragment: Fragment = when (tab.position) {
                    0 -> RaspadinhaAdminFragment()
                    1 -> PerguntaAdminFragment()
                    2 -> DesafioAdminFragment()
                    3 -> PunicaoAdminFragment()
                    else -> RaspadinhaAdminFragment()
                }
                replaceFragment(fragment)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
