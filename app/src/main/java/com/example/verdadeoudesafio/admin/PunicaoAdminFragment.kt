package com.example.verdadeoudesafio.admin

import androidx.room.Room
import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.data.entity.PunicaoEntity

// 1. Mude para herdar de BaseAdminFragment
class PunicaoAdminFragment : BaseAdminFragment() {

    // 2. Adicione a instância do DB (com o NOME CORRETO)
    private val db by lazy {
        Room.databaseBuilder(requireContext(), AppDatabase::class.java, "verdade_ou_desafio_db")
            .build()
    }

    // 3. Implemente os métodos para gerenciar Punições
    override suspend fun loadItems(): List<String> {
        return db.punicaoDao().getAll().map { it.texto }
    }

    override suspend fun addItem(text: String) {
        db.punicaoDao().insert(PunicaoEntity(texto = text, level = 1))
    }

    override suspend fun editItem(index: Int, text: String) {
        val list = db.punicaoDao().getAll()
        val item = list[index]
        db.punicaoDao().update(item.copy(texto = text))
    }

    override suspend fun deleteItem(index: Int) {
        val list = db.punicaoDao().getAll()
        db.punicaoDao().delete(list[index])
    }
}