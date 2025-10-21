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
import com.example.verdadeoudesafio.R // Importe seu R
import com.example.verdadeoudesafio.data.entity.DesafioEntity // Importa DesafioEntity para verificar tipo
import com.example.verdadeoudesafio.databinding.FragmentAdminSimpleBinding // Use seu ViewBinding correto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseAdminFragment : Fragment() {

    private var _binding: FragmentAdminSimpleBinding? = null
    private val binding get() = _binding!!

    // Adapter atualizado
    private lateinit var adapter: AdminItemAdapter

    // Funções abstratas agora usam TextLevelItem e incluem parâmetros necessários
    abstract suspend fun loadItems(): List<TextLevelItem>
    abstract suspend fun addItem(text: String, level: Int, tempo: Int?) // Adicionar precisa de level e tempo opcional
    abstract suspend fun editItem(item: TextLevelItem, newText: String, newLevel: Int, newTempo: Int?) // Editar pode mudar tudo
    abstract suspend fun deleteItem(item: TextLevelItem)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminSimpleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupAddButton()
        loadItemsToList() // Carrega os itens na criação da view
    }

    private fun setupRecyclerView() {
        adapter = AdminItemAdapter(
            // Callbacks agora recebem TextLevelItem
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

    // Carrega e atualiza a lista na UI
    private fun loadItemsToList() {
        lifecycleScope.launch(Dispatchers.IO) { // Roda em background
            val items = loadItems() // Chama a implementação do fragment filho
            withContext(Dispatchers.Main) { // Volta para a thread principal
                adapter.updateList(items) // Atualiza o adapter com a lista agrupada
            }
        }
    }

    // --- DIÁLOGOS ATUALIZADOS ---

    private fun showAddDialog() {
        val inflater = LayoutInflater.from(requireContext())
        // Infla o novo layout de diálogo
        val view = inflater.inflate(R.layout.dialog_add_edit_item, null)
        val editText = view.findViewById<EditText>(R.id.editTextItem)
        val levelRadioGroup = view.findViewById<RadioGroup>(R.id.radioGroupLevel)
        val timeInputLayout = view.findViewById<View>(R.id.timeInputLayout)
        val editTempoMin = view.findViewById<EditText>(R.id.editTempoMin)
        val editTempoSeg = view.findViewById<EditText>(R.id.editTempoSeg)

        // Pré-seleciona o nível Moderado (2)
        view.findViewById<RadioButton>(R.id.radioLevel2).isChecked = true

        // Mostra campos de tempo apenas se este fragment for de Desafios
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
                    else -> 2 // Default para Moderado se nada selecionado
                }
                var tempo: Int? = null
                if (this is DesafioAdminFragment) { // Calcula o tempo apenas para desafios
                    val min = editTempoMin.text.toString().toIntOrNull() ?: 0
                    val seg = editTempoSeg.text.toString().toIntOrNull() ?: 0
                    tempo = (min * 60) + seg
                }

                if (text.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        addItem(text, selectedLevel, tempo) // Passa texto, nível e tempo (pode ser null)
                        withContext(Dispatchers.Main) { loadItemsToList() } // Recarrega a lista
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditDialog(item: TextLevelItem) {
        val inflater = LayoutInflater.from(requireContext())
        // Reutiliza o mesmo layout de diálogo
        val view = inflater.inflate(R.layout.dialog_add_edit_item, null)
        val editText = view.findViewById<EditText>(R.id.editTextItem)
        val levelRadioGroup = view.findViewById<RadioGroup>(R.id.radioGroupLevel)
        val timeInputLayout = view.findViewById<View>(R.id.timeInputLayout)
        val editTempoMin = view.findViewById<EditText>(R.id.editTempoMin)
        val editTempoSeg = view.findViewById<EditText>(R.id.editTempoSeg)

        // Preenche os campos com os dados do item existente
        editText.setText(item.texto)
        when (item.level) {
            1 -> view.findViewById<RadioButton>(R.id.radioLevel1).isChecked = true
            2 -> view.findViewById<RadioButton>(R.id.radioLevel2).isChecked = true
            3 -> view.findViewById<RadioButton>(R.id.radioLevel3).isChecked = true
        }

        // Mostra e preenche o tempo se for um Desafio
        if (item is DesafioEntity) {
            timeInputLayout.visibility = View.VISIBLE
            val totalSeconds = item.tempo // A interface garante que tempo não é null aqui
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
                    else -> item.level // Mantém o nível original se nada for selecionado
                }
                var newTempo: Int? = null
                if (item is DesafioEntity) { // Calcula o novo tempo apenas para desafios
                    val min = editTempoMin.text.toString().toIntOrNull() ?: 0
                    val seg = editTempoSeg.text.toString().toIntOrNull() ?: 0
                    newTempo = (min * 60) + seg
                }

                if (newText.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        editItem(item, newText, newLevel, newTempo) // Passa o item original e os novos valores
                        withContext(Dispatchers.Main) { loadItemsToList() } // Recarrega a lista
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmation(item: TextLevelItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja deletar este item?\n\n\"${item.texto}\"") // Mostra o texto para confirmação
            .setPositiveButton("Deletar") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    deleteItem(item) // Passa o item original para deleção
                    withContext(Dispatchers.Main) { loadItemsToList() } // Recarrega a lista
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpa a referência ao binding para evitar memory leaks
    }
}