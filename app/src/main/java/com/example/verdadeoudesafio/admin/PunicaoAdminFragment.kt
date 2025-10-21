package com.example.verdadeoudesafio.admin

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.verdadeoudesafio.R
import com.example.verdadeoudesafio.data.entity.PunicaoEntity
import com.example.verdadeoudesafio.databinding.DialogAddEditItemBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PunicaoAdminFragment : BaseAdminFragment<PunicaoEntity>() {

    override val fragmentTitle: String
        get() = "Gerenciar Punições"

    private val punicaoDao by lazy { db.punicaoDao() }

    override fun setupViewModelObservers() {
        lifecycleScope.launch {
            punicaoDao.getAllFlow().collectLatest { punicoes ->
                adapter.submitList(punicoes)
            }
        }
    }

    override fun setupViews() {
        binding.btnAddItem.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    override suspend fun deleteItemFromDb(item: PunicaoEntity) {
        punicaoDao.delete(item)
    }

    override fun showAddEditDialog(item: PunicaoEntity?) {
        val dialogBinding = DialogAddEditItemBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (item == null) "Adicionar Punição" else "Editar Punição")
            .setView(dialogBinding.root)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        // Esconde o campo de tempo
        dialogBinding.tilTempo.visibility = View.GONE

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
                    return@setOnClickListener
                }

                val newItem = PunicaoEntity(
                    id = item?.id ?: 0,
                    text = texto,
                    level = level
                )

                lifecycleScope.launch {
                    if (item == null) {
                        punicaoDao.insert(newItem)
                    } else {
                        punicaoDao.update(newItem)
                    }
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }
}