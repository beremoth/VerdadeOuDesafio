package com.example.verdadeoudesafio.admin

import androidx.room.Room
import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.data.entity.PerguntaEntity

class PerguntaAdminFragment : BaseAdminFragment() {

    private val db by lazy {
        Room.databaseBuilder(requireContext(), AppDatabase::class.java, "verdade_ou_desafio_db").build()
    }

    // Sobrescreve para definir o título
    override fun getFragmentTitle(): String = "Gerenciar Perguntas"

    // Retorna a lista de TextLevelItem
    override suspend fun loadItems(): List<TextLevelItem> {
        return db.perguntaDao().getAll() // O DAO retorna List<PerguntaEntity>, que implementa TextLevelItem
    }

    // Implementa addItem com os novos parâmetros
    override suspend fun addItem(text: String, level: Int, tempo: Int?) { // tempo é ignorado aqui
        db.perguntaDao().insert(PerguntaEntity(texto = text, level = level)) // [cite: 21]
    }

    // Implementa editItem com os novos parâmetros
    override suspend fun editItem(item: TextLevelItem, newText: String, newLevel: Int, newTempo: Int?) { // newLevel e newTempo são ignorados aqui
        if (item is PerguntaEntity) { // Verifica se o item é do tipo correto
            // Cria uma cópia com o novo texto, mas mantém o ID e nível originais
            // Se quiser permitir editar o nível, use: item.copy(texto = newText, level = newLevel)
            val updatedItem = item.copy(texto = newText, level = newLevel)
            db.perguntaDao().update(updatedItem)
        }
    }

    // Implementa deleteItem com TextLevelItem
    override suspend fun deleteItem(item: TextLevelItem) {
        if (item is PerguntaEntity) {
            db.perguntaDao().delete(item)
        }
    }
}