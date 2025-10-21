package com.example.verdadeoudesafio.data.dao

import androidx.room.*
import com.example.verdadeoudesafio.data.entity.PunicaoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PunicaoDao {
    @Query("SELECT * FROM punicoes")
    suspend fun getAll(): List<PunicaoEntity>

    // --- FUNÇÃO ADICIONADA --- (Para o Admin)
    @Query("SELECT * FROM punicoes ORDER BY level, texto")
    fun getAllFlow(): Flow<List<PunicaoEntity>>

    @Query("SELECT * FROM punicoes WHERE level = :level")
    suspend fun getByLevel(level: Int): List<PunicaoEntity>

    // --- FUNÇÃO ADICIONADA --- (Para o Jogo)
    @Query("SELECT * FROM punicoes WHERE level <= :level ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomByLevel(level: Int): PunicaoEntity? // Estava retornando PerguntaEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Adicionado 'onConflict'
    suspend fun insert(punicoes: PunicaoEntity)

    @Update
    suspend fun update(punicoes: PunicaoEntity)

    @Delete
    suspend fun delete(punicoes: PunicaoEntity)

    @Query("DELETE FROM punicoes WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM punicoes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(id) FROM punicoes")
    suspend fun count(): Int
}