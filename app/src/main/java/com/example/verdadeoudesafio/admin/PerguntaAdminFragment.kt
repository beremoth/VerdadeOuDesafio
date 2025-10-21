package com.example.verdadeoudesafio.admin

import android.view.LayoutInflater
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

    // 2. Implementa 'setupViewModelObservers' (agora 'setupObservers')
    override fun setupViewModelObservers() {
        lifecycleScope.launch {
            perguntaDao.getAllFlow().collectLatest { perguntas ->
                adapter.submitList(perguntas)
            }
        }
    }

    // 3. Implementa 'setupViews'
    override fun setupViews() {
        binding.btnAddItem.setOnClickListener {
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
        dialogBinding.tilTempo.visibility = View.GONE

        // Preenche os dados se for edição
        item?.let {
            dialogBinding.etTexto.setText(it.text)
            val radioId = when (it.level) {
                1 -> R.id.rbLeve
                2 -> R.id.rbModerado
                else -> R.id.rbExtremo
            }
            dialogBinding.radioGroupLevel.check(radioId)
        }

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val texto = dialogBinding.etTexto.text.toString().trim()
                val level = when (dialogBinding.radioGroupLevel.checkedRadioButtonId) {
                    R.id.rbLeve -> 1
                    R.id.rbModerado -> 2
                    R.id.rbExtremo -> 3
                    else -> 0
                }

                if (texto.isEmpty()) {
                    dialogBinding.etTexto.error = "O texto não pode ser vazio"
                    return@setOnClickListener
                }
                if (level == 0) {
                    // (Opcional) Mostrar erro se nenhum nível for selecionado
                    return@setOnClickListener
                }

                val newItem = PerguntaEntity(
                    id = item?.id ?: 0,
                    text = texto,
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