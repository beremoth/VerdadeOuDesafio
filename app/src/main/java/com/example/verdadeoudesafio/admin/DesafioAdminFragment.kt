package com.example.verdadeoudesafio.admin

import androidx.room.Room
import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.data.entity.DesafioEntity

class DesafioAdminFragment : BaseAdminFragment() { // [cite: 22]

    private val db by lazy {
        Room.databaseBuilder(requireContext(), AppDatabase::class.java, "verdade_ou_desafio_db")
            .build()
    }

    // Sobrescreve para definir o título
    override fun getFragmentTitle(): String = "Gerenciar Desafios"

    // Retorna a lista de TextLevelItem
    override suspend fun loadItems(): List<TextLevelItem> {
        return db.desafioDao().getAll() // Retorna List<DesafioEntity>
    }

    // Implementa addItem com os novos parâmetros, usando o tempo
    override suspend fun addItem(text: String, level: Int, tempo: Int?) {
        // Usa o tempo recebido, ou 0 se for nulo
        db.desafioDao().insert(DesafioEntity(texto = text, level = level, tempo = tempo ?: 0)) // [cite: 23]
    }

    // Implementa editItem com os novos parâmetros, incluindo tempo
    override suspend fun editItem(item: TextLevelItem, newText: String, newLevel: Int, newTempo: Int?) {
        if (item is DesafioEntity) {
            // Cria cópia com novo texto, nível e tempo (usando o tempo original se newTempo for nulo)
            val updatedItem = item.copy(texto = newText, level = newLevel, tempo = newTempo ?: item.tempo)
            db.desafioDao().update(updatedItem)
        }
    }

    // Implementa deleteItem com TextLevelItem
    override suspend fun deleteItem(item: TextLevelItem) {
        if (item is DesafioEntity) {
            db.desafioDao().delete(item) // [cite: 24]
        }
    }
}