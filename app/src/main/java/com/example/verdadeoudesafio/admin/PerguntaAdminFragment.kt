package com.example.verdadeoudesafio.admin

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.verdadeoudesafio.R
import com.example.verdadeoudesafio.data.entity.PerguntaEntity
import com.example.verdadeoudesafio.databinding.DialogAddEditItemBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PerguntaAdminFragment : BaseAdminFragment<PerguntaEntity>() {

    // 1. Implementa o 'fragmentTitle'
    override val fragmentTitle: String
        get() = "Gerenciar Perguntas"

    private val perguntaDao by lazy { db.perguntaDao() }

    // 2. Implementa
    override fun setupObservers() {
        lifecycleScope.launch {
            perguntaDao.getAllFlow().collectLatest { perguntas -> // Unresolved reference 'getAllFlow'.
                adapter.submitList(perguntas)
            }
        }
    }

    // 3. Implementa 'setupViews'
    override fun setupViews() {
        binding.btnAdd.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    // 4. Implementa 'deleteItemFromDb'
    override suspend fun deleteItemFromDb(item: PerguntaEntity) {
        perguntaDao.delete(item)
    }

    // 5. Implementa 'showAddEditDialog'
    override fun showAddEditDialog(item: PerguntaEntity?) {
        val dialogBinding = DialogAddEditItemBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (item == null) "Adicionar Pergunta" else "Editar Pergunta")
            .setView(dialogBinding.root)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        // Esconde o campo de tempo, pois Pergunta não usa
        dialogBinding.timeInputLayout.visibility = View.GONE

        // Preenche os dados se for edição
        item?.let {
            dialogBinding.editTextItem.setText(it.texto)
            val radioId = when (it.level) {
                R.id.radioLevel1 -> 1
                R.id.radioLevel2 -> 2
                R.id.radioLevel3 -> 3
                else -> 0
            }
            dialogBinding.radioGroupLevel.check(radioId)
        }

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val texto = dialogBinding.editTextItem.text.toString().trim()
                val level = when (dialogBinding.radioGroupLevel.checkedRadioButtonId) {
                    R.id.radioLevel1 -> 1
                    R.id.radioLevel2 -> 2
                    R.id.radioLevel3 -> 3
                    else -> 0
                }

                if (texto.isEmpty()) {
                    dialogBinding.editTextItem.error = "O texto não pode ser vazio"
                    return@setOnClickListener
                }
                if (level == 0) {
                    // (Opcional) Mostrar erro se nenhum nível for selecionado
                    return@setOnClickListener
                }

                val newItem = PerguntaEntity(
                    id = item?.id ?: 0,
                    texto = texto,
                    level = level
                )

                lifecycleScope.launch {
                    if (item == null) {
                        perguntaDao.insert(newItem)
                    } else {
                        perguntaDao.update(newItem)
                    }
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}