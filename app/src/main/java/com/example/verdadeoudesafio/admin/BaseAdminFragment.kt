package com.example.verdadeoudesafio.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.verdadeoudesafio.databinding.FragmentCrudBinding
import kotlinx.coroutines.launch

abstract class BaseAdminFragment : Fragment() {

    protected lateinit var binding: FragmentCrudBinding
    protected lateinit var adapter: AdminItemAdapter

    abstract suspend fun loadItems(): List<String>
    abstract suspend fun addItem(text: String)
    abstract suspend fun editItem(index: Int, text: String)
    abstract suspend fun deleteItem(index: Int)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCrudBinding.inflate(inflater, container, false)
        setupRecycler()
        setupButtons()
        refreshList()
        return binding.root
    }

    private fun setupRecycler() {
        adapter = AdminItemAdapter(mutableListOf(),
            onEdit = { pos, text -> showEditDialog(pos, text) },
            onDelete = { pos -> lifecycleScope.launch { deleteItem(pos); refreshList() } }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnAdd.setOnClickListener {
            val input = binding.etNewItem.text.toString()
            if (input.isNotBlank()) {
                lifecycleScope.launch {
                    addItem(input)
                    refreshList()
                    binding.etNewItem.text.clear()
                }
            }
        }
    }

    protected fun showEditDialog(index: Int, oldText: String) {
        val editText = EditText(requireContext())
        editText.setText(oldText)
        AlertDialog.Builder(requireContext())
            .setTitle("Editar item")
            .setView(editText)
            .setPositiveButton("Salvar") { _, _ ->
                val newText = editText.text.toString()
                lifecycleScope.launch {
                    editItem(index, newText)
                    refreshList()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    protected fun refreshList() {
        lifecycleScope.launch {
            adapter.updateList(loadItems())
        }
    }
}
