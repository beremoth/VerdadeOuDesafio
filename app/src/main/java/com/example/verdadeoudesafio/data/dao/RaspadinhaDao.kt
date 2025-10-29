package com.example.verdadeoudesafio.data.dao

import androidx.room.*
import com.example.verdadeoudesafio.data.entity.RaspadinhaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RaspadinhaDao {
    @Query("SELECT * FROM raspadinhas")
    suspend fun getAll(): List<RaspadinhaEntity>

    // --- FUNÇÃO ADICIONADA --- (Para o Admin)
    @Query("SELECT * FROM raspadinhas")
    fun getAllFlow(): Flow<List<RaspadinhaEntity>>

    // --- FUNÇÃO ADICIONADA --- (Para o Jogo)
    @Query("SELECT * FROM raspadinhas ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandom(): RaspadinhaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(raspadinha: RaspadinhaEntity)

    @Update
    suspend fun update(raspadinha: RaspadinhaEntity)

    @Delete
    suspend fun delete(raspadinha: RaspadinhaEntity)

    @Query("DELETE FROM raspadinhas")
    suspend fun deleteAll()

    // --- FUNÇÃO ADICIONADA --- (Corrige o DatabaseInitializer)
    @Query("SELECT COUNT(id) FROM raspadinhas")
    suspend fun count(): Int

    @Query("SELECT imagePath FROM raspadinhas")
    suspend fun getAllPaths(): List<String>
}