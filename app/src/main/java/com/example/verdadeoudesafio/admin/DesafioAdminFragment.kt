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

    override fun setupObservers() {
        lifecycleScope.launch {
            desafioDao.getAllFlow().collectLatest { desafios ->
                adapter.submitList(desafios)
            }
        }
    }

    override fun setupViews() {
        binding.btnAdd.setOnClickListener {
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
        dialogBinding.timeInputLayout.visibility = View.VISIBLE

        item?.let {
            dialogBinding.editTextItem.setText(it.texto)
            // ✅ Correto: converte tempo total em minutos e segundos
            val totalSeconds = it.tempo ?: 0
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60

            dialogBinding.editTempoMin.setText(if (minutes > 0) minutes.toString() else "")
            dialogBinding.editTempoSeg.setText(if (seconds > 0) seconds.toString() else "")

           val radioId = when (it.level) {
                1 -> R.id.radioLevel1
                2 -> R.id.radioLevel2
                else -> R.id.radioLevel3
            }
            dialogBinding.radioGroupLevel.check(radioId)
        }

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val texto = dialogBinding.editTextItem.text.toString().trim()
                val minText = dialogBinding.editTempoMin.text.toString().trim()
                val secText = dialogBinding.editTempoSeg.text.toString().trim()

                val minutes = if (minText.isEmpty()) 0 else minText.toIntOrNull() ?: 0
                val seconds = if (secText.isEmpty()) 0 else secText.toIntOrNull() ?: 0

                val tempo = minutes * 60 + seconds

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

                val newItem = DesafioEntity(
                    id = item?.id ?: 0,
                    texto = texto,
                    level = level,
                    tempo = tempo
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