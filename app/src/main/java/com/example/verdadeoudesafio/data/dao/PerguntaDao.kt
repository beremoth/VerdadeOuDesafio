package com.example.verdadeoudesafio.data.dao

import androidx.room.*
import com.example.verdadeoudesafio.data.entity.PerguntaEntity

@Dao
interface PerguntaDao {

    // 🔹 Retorna todas as perguntas
    @Query("SELECT * FROM perguntas")
    suspend fun getAll(): List<PerguntaEntity>

    // 🔹 Insere nova pergunta (ou substitui se já existir o mesmo ID)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pergunta: PerguntaEntity)

    // 🔹 Atualiza pergunta existente
    @Update
    suspend fun update(pergunta: PerguntaEntity)

    // 🔹 Deleta pergunta específica
    @Delete
    suspend fun delete(pergunta: PerguntaEntity)

    // 🔹 Deleta pergunta pelo ID
    @Query("DELETE FROM perguntas WHERE id = :id")
    suspend fun deleteById(id: Int)

    // 🔹 Deleta todas as perguntas
    @Query("DELETE FROM perguntas")
    suspend fun deleteAll()
}
