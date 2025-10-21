package com.example.verdadeoudesafio.admin

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.verdadeoudesafio.R
import com.example.verdadeoudesafio.data.entity.DesafioEntity
import com.example.verdadeoudesafio.databinding.DialogAddEditItemBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DesafioAdminFragment : BaseAdminFragment<DesafioEntity>() {

    override val fragmentTitle: String
        get() = "Gerenciar Desafios"

    private val desafioDao by lazy { db.desafioDao() }

    override fun setupViewModelObservers() {
        lifecycleScope.launch {
            desafioDao.getAllFlow().collectLatest { desafios ->
                adapter.submitList(desafios)
            }
        }
    }

    override fun setupViews() {
        binding.btnAddItem.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    override suspend fun deleteItemFromDb(item: DesafioEntity) {
        desafioDao.delete(item)
    }

    override fun showAddEditDialog(item: DesafioEntity?) {
        val dialogBinding = DialogAddEditItemBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (item == null) "Adicionar Desafio" else "Editar Desafio")
            .setView(dialogBinding.root)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        // Desafio usa o campo de tempo
        dialogBinding.tilTempo.visibility = View.VISIBLE

        item?.let {
            dialogBinding.etTexto.setText(it.text)
            dialogBinding.etTempo.setText(it.time?.toString() ?: "0")
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
                val tempoStr = dialogBinding.etTempo.text.toString()
                val tempo = if (tempoStr.isEmpty()) 0 else tempoStr.toIntOrNull() ?: 0

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
                    return@setOnClickListener
                }

                val newItem = DesafioEntity(
                    id = item?.id ?: 0,
                    text = texto,
                    level = level,
                    time = tempo
                )

                lifecycleScope.launch {
                    if (item == null) {
                        desafioDao.insert(newItem)
                    } else {
                        desafioDao.update(newItem)
                    }
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }
}