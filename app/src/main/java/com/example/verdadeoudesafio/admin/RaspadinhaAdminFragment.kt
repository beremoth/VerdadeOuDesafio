package com.example.verdadeoudesafio.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.verdadeoudesafio.databinding.FragmentAdminSimpleBinding

class RaspadinhaAdminFragment : Fragment() {
    private var _binding: FragmentAdminSimpleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminSimpleBinding.inflate(inflater, container, false)
        binding.textTitle.text = "Gerenciar Raspadinhas"
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
