package com.example.verdadeoudesafio.data.dao

import androidx.room.*
import com.example.verdadeoudesafio.data.entity.PerguntaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PerguntaDao {
    @Query("SELECT * FROM perguntas")
    suspend fun getAll(): List<PerguntaEntity>

    @Query("SELECT * FROM perguntas ORDER BY level, texto")
    fun getAllFlow(): Flow<List<PerguntaEntity>>

    // --- CORRIGIDO O TIPO DE RETORNO ---
    @Query("SELECT * FROM perguntas ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandom(): PerguntaEntity?

    // --- ADICIONADA FUNÇÃO FALTANTE ---
    @Query("SELECT * FROM perguntas WHERE level = :level ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomByLevel(level: Int): PerguntaEntity?

    @Query("SELECT COUNT(id) FROM perguntas")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(perguntas: PerguntaEntity)

    @Query("SELECT * FROM perguntas WHERE level = :level")
    suspend fun getByLevel(level: Int): List<PerguntaEntity>

    @Update
    suspend fun update(perguntas: PerguntaEntity)

    @Delete
    suspend fun delete(perguntas: PerguntaEntity)

    @Query("DELETE FROM perguntas WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM perguntas")
    suspend fun deleteAll()

    @Query("SELECT EXISTS(SELECT 1 FROM perguntas WHERE texto = :text)")
    suspend fun existsByText(text: String): Boolean

    @Query("SELECT texto FROM perguntas")
    suspend fun getAllTexts(): List<String>

}