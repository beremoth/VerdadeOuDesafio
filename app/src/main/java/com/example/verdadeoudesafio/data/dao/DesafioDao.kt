package com.example.verdadeoudesafio.data.dao

import androidx.room.*
import com.example.verdadeoudesafio.data.entity.DesafioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DesafioDao {
    @Query("SELECT * FROM desafios")
    suspend fun getAll(): List<DesafioEntity>

    @Query("SELECT * FROM desafios ORDER BY level, texto")
    fun getAllFlow(): Flow<List<DesafioEntity>>

    @Query("SELECT * FROM desafios WHERE level = :level")
    suspend fun getByLevel(level: Int): List<DesafioEntity>

    @Query("SELECT * FROM desafios ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandom(): DesafioEntity?

    // --- ADICIONADA FUNÇÃO FALTANTE ---
    @Query("SELECT * FROM desafios WHERE level = :level ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomByLevel(level: Int): DesafioEntity?

    @Insert
    suspend fun insert(desafio: DesafioEntity)

    @Update
    suspend fun update(desafio: DesafioEntity)

    @Delete
    suspend fun delete(desafio: DesafioEntity)

    @Query("DELETE FROM desafios WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM desafios")
    suspend fun deleteAll()

    @Query("SELECT COUNT(id) FROM desafios")
    suspend fun count(): Int
}