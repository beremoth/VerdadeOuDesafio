package com.example.verdadeoudesafio.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.verdadeoudesafio.R // Importa R
import com.example.verdadeoudesafio.data.entity.DesafioEntity // Importa DesafioEntity
import com.example.verdadeoudesafio.databinding.FragmentAdminSimpleBinding // Binding CORRETO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseAdminFragment : Fragment() {

    private var _binding: FragmentAdminSimpleBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AdminItemAdapter

    // Funções abstratas atualizadas
    abstract suspend fun loadItems(): List<TextLevelItem>
    abstract suspend fun addItem(text: String, level: Int, tempo: Int?)
    abstract suspend fun editItem(item: TextLevelItem, newText: String, newLevel: Int, newTempo: Int?)
    abstract suspend fun deleteItem(item: TextLevelItem)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminSimpleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupAddButton()
        loadItemsToList()
        // Define o título do fragment
        binding.fragmentTitle.text = getFragmentTitle()
    }

    // --- FUNÇÃO CORRIGIDA ---
    // Adicionado 'open' para permitir que as classes filhas a sobrescrevam
    open fun getFragmentTitle(): String = "Gerenciar Itens"
    // -------------------------

    private fun setupRecyclerView() {
        adapter = AdminItemAdapter(
            onEdit = { item -> showEditDialog(item) },
            onDelete = { item -> showDeleteConfirmation(item) }
        )
        binding.recyclerViewItems.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewItems.adapter = adapter
    }

    private fun setupAddButton() {
        binding.btnAddItem.setOnClickListener {
            showAddDialog()
        }
    }

    private fun loadItemsToList() {
        lifecycleScope.launch(Dispatchers.IO) {
            val items = loadItems()
            withContext(Dispatchers.Main) {
                adapter.updateList(items)
            }
        }
    }

    // --- DIÁLOGOS ATUALIZADOS ---

    private fun showAddDialog() {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_add_edit_item, null) // Usa o layout do diálogo
        val editText = view.findViewById<EditText>(R.id.editTextItem)
        val levelRadioGroup = view.findViewById<RadioGroup>(R.id.radioGroupLevel)
        val timeInputLayout = view.findViewById<View>(R.id.timeInputLayout)
        val editTempoMin = view.findViewById<EditText>(R.id.editTempoMin)
        val editTempoSeg = view.findViewById<EditText>(R.id.editTempoSeg)

        // Pré-seleciona Moderado
        view.findViewById<RadioButton>(R.id.radioLevel2).isChecked = true

        // Mostra tempo apenas para DesafioAdminFragment
        timeInputLayout.visibility = if (this is DesafioAdminFragment) View.VISIBLE else View.GONE

        AlertDialog.Builder(requireContext())
            .setTitle("Adicionar Novo Item")
            .setView(view)
            .setPositiveButton("Adicionar") { _, _ ->
                val text = editText.text.toString().trim()
                val selectedLevel = when (levelRadioGroup.checkedRadioButtonId) {
                    R.id.radioLevel1 -> 1
                    R.id.radioLevel2 -> 2
                    R.id.radioLevel3 -> 3
                    else -> 2 // Default para Moderado
                }
                var tempo: Int? = null
                if (this is DesafioAdminFragment) {
                    val min = editTempoMin.text.toString().toIntOrNull() ?: 0
                    val seg = editTempoSeg.text.toString().toIntOrNull() ?: 0
                    tempo = (min * 60) + seg
                }

                if (text.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        addItem(text, selectedLevel, tempo)
                        withContext(Dispatchers.Main) { loadItemsToList() }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditDialog(item: TextLevelItem) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_add_edit_item, null)
        val editText = view.findViewById<EditText>(R.id.editTextItem)
        val levelRadioGroup = view.findViewById<RadioGroup>(R.id.radioGroupLevel)
        val timeInputLayout = view.findViewById<View>(R.id.timeInputLayout)
        val editTempoMin = view.findViewById<EditText>(R.id.editTempoMin)
        val editTempoSeg = view.findViewById<EditText>(R.id.editTempoSeg)

        editText.setText(item.texto)
        when (item.level) {
            1 -> view.findViewById<RadioButton>(R.id.radioLevel1).isChecked = true
            2 -> view.findViewById<RadioButton>(R.id.radioLevel2).isChecked = true
            3 -> view.findViewById<RadioButton>(R.id.radioLevel3).isChecked = true
        }

        if (item is DesafioEntity) {
            timeInputLayout.visibility = View.VISIBLE
            val totalSeconds = item.tempo
            editTempoMin.setText((totalSeconds / 60).toString())
            editTempoSeg.setText((totalSeconds % 60).toString())
        } else {
            timeInputLayout.visibility = View.GONE
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Item")
            .setView(view)
            .setPositiveButton("Salvar") { _, _ ->
                val newText = editText.text.toString().trim()
                val newLevel = when (levelRadioGroup.checkedRadioButtonId) {
                    R.id.radioLevel1 -> 1
                    R.id.radioLevel2 -> 2
                    R.id.radioLevel3 -> 3
                    else -> item.level // Mantém o nível original
                }
                var newTempo: Int? = null
                if (item is DesafioEntity) {
                    val min = editTempoMin.text.toString().toIntOrNull() ?: 0
                    val seg = editTempoSeg.text.toString().toIntOrNull() ?: 0
                    newTempo = (min * 60) + seg
                }

                if (newText.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        editItem(item, newText, newLevel, newTempo)
                        withContext(Dispatchers.Main) { loadItemsToList() }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmation(item: TextLevelItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja deletar este item?\n\n\"${item.texto}\"")
            .setPositiveButton("Deletar") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    deleteItem(item)
                    withContext(Dispatchers.Main) { loadItemsToList() }
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