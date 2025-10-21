package com.example.verdadeoudesafio.admin

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AdminPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        PerguntaAdminFragment(),
        DesafioAdminFragment(),
        PunicaoAdminFragment(),
        RaspadinhaAdminFragment()

    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}