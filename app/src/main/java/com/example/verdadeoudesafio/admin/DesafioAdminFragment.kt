package com.example.verdadeoudesafio.admin

import androidx.room.Room
import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.data.entity.DesafioEntity

// 1. Mude para herdar de BaseAdminFragment
class DesafioAdminFragment : BaseAdminFragment() {

    // 2. Adicione a instância do DB (com o NOME CORRETO)
    private val db by lazy {
        Room.databaseBuilder(requireContext(), AppDatabase::class.java, "verdade_ou_desafio_db")
            .build()
    }

    // 3. Implemente os métodos para gerenciar Desafios
    override suspend fun loadItems(): List<String> {
        return db.desafioDao().getAll().map { it.texto }
    }

    override suspend fun addItem(text: String) {
        // Você pode ajustar o level/tempo padrão aqui se quiser
        db.desafioDao().insert(DesafioEntity(texto = text, level = 1, tempo = 0))
    }

    override suspend fun editItem(index: Int, text: String) {
        val list = db.desafioDao().getAll()
        val item = list[index]
        db.desafioDao().update(item.copy(texto = text))
    }

    override suspend fun deleteItem(index: Int) {
        val list = db.desafioDao().getAll()
        db.desafioDao().delete(list[index])
    }
}