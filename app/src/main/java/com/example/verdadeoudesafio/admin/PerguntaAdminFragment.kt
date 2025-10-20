package com.example.verdadeoudesafio.admin

import androidx.room.Room
import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.data.entity.PerguntaEntity

class PerguntaAdminFragment : BaseAdminFragment() {

    private val db by lazy {
        Room.databaseBuilder(requireContext(), AppDatabase::class.java, "game.db").build()
    }

    override suspend fun loadItems(): List<String> {
        return db.perguntaDao().getAll().map { it.texto }
    }

    override suspend fun addItem(text: String) {
        db.perguntaDao().insert(PerguntaEntity(texto = text, level = 1))
    }

    override suspend fun editItem(index: Int, text: String) {
        val list = db.perguntaDao().getAll()
        val item = list[index]
        db.perguntaDao().update(item.copy(texto = text))
    }

    override suspend fun deleteItem(index: Int) {
        val list = db.perguntaDao().getAll()
        db.perguntaDao().delete(list[index])
    }
}
