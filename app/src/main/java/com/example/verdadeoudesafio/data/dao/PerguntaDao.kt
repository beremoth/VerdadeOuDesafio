package com.example.verdadeoudesafio.data.dao

import androidx.room.*
import com.example.verdadeoudesafio.data.entity.PerguntaEntity
import com.example.verdadeoudesafio.data.entity.PunicaoEntity

@Dao
interface PerguntaDao {

    // 🔹 Retorna todas as perguntas
    @Query("SELECT * FROM perguntas")
    suspend fun getAll(): List<PerguntaEntity>

    // 🔹 Insere nova pergunta (ou substitui se já existir o mesmo ID)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(perguntas: PerguntaEntity)

    @Query("SELECT * FROM perguntas WHERE level = :level")
    suspend fun getByLevel(level: Int): List<PerguntaEntity>

    // 🔹 Atualiza pergunta existente
    @Update
    suspend fun update(perguntas: PerguntaEntity)

    // 🔹 Deleta pergunta específica
    @Delete
    suspend fun delete(perguntas: PerguntaEntity)

    // 🔹 Deleta pergunta pelo ID
    @Query("DELETE FROM perguntas WHERE id = :id")
    suspend fun deleteById(id: Int)

    // 🔹 Deleta todas as perguntas
    @Query("DELETE FROM perguntas")
    suspend fun deleteAll()
}
