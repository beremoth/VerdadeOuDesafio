package com.example.verdadeoudesafio.data.dao

import androidx.room.*
import com.example.verdadeoudesafio.data.entity.RaspadinhaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RaspadinhaDao {
    @Query("SELECT * FROM raspadinhas")
    suspend fun getAll(): List<RaspadinhaEntity>

    // --- ADICIONADA FUNÇÃO FALTANTE ---
    @Query("SELECT * FROM raspadinhas")
    fun getAllFlow(): Flow<List<RaspadinhaEntity>>

    // --- ADICIONADA FUNÇÃO FALTANTE ---
    @Query("SELECT * FROM raspadinhas ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandom(): RaspadinhaEntity?

    @Insert
    suspend fun insert(raspadinha: RaspadinhaEntity)

    @Update
    suspend fun update(raspadinha: RaspadinhaEntity)

    @Delete
    suspend fun delete(raspadinha: RaspadinhaEntity)

    @Query("DELETE FROM raspadinhas WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(id) FROM raspadinhas")
    suspend fun count(): Int
}