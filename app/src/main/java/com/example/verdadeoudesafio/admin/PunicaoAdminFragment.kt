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

    override fun setupObservers() {
        lifecycleScope.launch {
            punicaoDao.getAllFlow().collectLatest { punicoes ->
                adapter.submitList(punicoes)
            }
        }
    }

    override fun setupViews() {
        binding.btnAdd.setOnClickListener {
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
        dialogBinding.timeInputLayout.visibility = View.GONE

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
                    return@setOnClickListener
                }

                val newItem = PunicaoEntity(
                    id = item?.id ?: 0,
                    texto = texto,
                    level = level
                    // Não há tempo para Punicao
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