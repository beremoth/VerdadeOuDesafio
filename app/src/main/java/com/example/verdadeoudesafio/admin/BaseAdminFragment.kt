package com.example.verdadeoudesafio.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.databinding.FragmentCrudBinding
import kotlinx.coroutines.launch


abstract class BaseAdminFragment<T : TextLevelItem> : Fragment() {

    private var _binding: FragmentCrudBinding? = null
    protected val binding get() = _binding!!


    protected lateinit var adapter: AdminItemAdapter<T>

    protected val db by lazy {
        AppDatabase.getDatabase(requireContext().applicationContext)
    }

    // Métodos abstratos que os "filhos" (PerguntaAdminFragment, etc.) vão implementar
    abstract val fragmentTitle: String
    abstract fun setupObservers()
    abstract fun setupViews()
    abstract fun showAddEditDialog(item: T?)
    abstract suspend fun deleteItemFromDb(item: T)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCrudBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragmentTitle.text = fragmentTitle

        setupRecyclerView()
        setupObservers()
        setupViews()
    }

    private fun setupRecyclerView() {
        adapter = AdminItemAdapter(
            onEdit = { item -> showAddEditDialog(item) },
            onDelete = { item -> showDeleteConfirmation(item) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    protected fun showDeleteConfirmation(item: T) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja deletar este item?\n\n\"${item.texto}\"")
            .setPositiveButton("Deletar") { _, _ ->
                lifecycleScope.launch {
                    deleteItemFromDb(item)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}