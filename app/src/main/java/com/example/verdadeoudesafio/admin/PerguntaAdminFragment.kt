package com.example.verdadeoudesafio.admin

import androidx.room.Room
import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.data.entity.PunicaoEntity

class PunicaoAdminFragment : BaseAdminFragment() {

    private val db by lazy {
        Room.databaseBuilder(requireContext(), AppDatabase::class.java, "verdade_ou_desafio_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    // Sobrescreve para definir o título
    override fun getFragmentTitle(): String = "Gerenciar Punições"

    // Retorna a lista de TextLevelItem
    override suspend fun loadItems(): List<TextLevelItem> {
        return db.punicaoDao().getAll() // Retorna List<PunicaoEntity> [cite: 19]
    }

    // Implementa addItem com os novos parâmetros
    override suspend fun addItem(text: String, level: Int, tempo: Int?) { // tempo é ignorado
        db.punicaoDao().insert(PunicaoEntity(texto = text, level = level))
    }

    // Implementa editItem com os novos parâmetros
    override suspend fun editItem(item: TextLevelItem, newText: String, newLevel: Int, newTempo: Int?) { // newLevel e newTempo são ignorados
        if (item is PunicaoEntity) {
            val updatedItem = item.copy(texto = newText, level = newLevel)
            db.punicaoDao().update(updatedItem)
        }
    }

    // Implementa deleteItem com TextLevelItem
    override suspend fun deleteItem(item: TextLevelItem) { // [cite: 20]
        if (item is PunicaoEntity) {
            db.punicaoDao().delete(item)
        }
    }
}