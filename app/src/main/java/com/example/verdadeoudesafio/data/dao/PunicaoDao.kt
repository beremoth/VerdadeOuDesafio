package com.example.verdadeoudesafio.data.dao

import androidx.room.*

import com.example.verdadeoudesafio.data.entity.PunicaoEntity

@Dao
interface PunicaoDao {
    @Query("SELECT * FROM punicoes ORDER BY timestamp DESC")
    suspend fun getAll(): List<PunicaoEntity>

    @Insert
    suspend fun insert(pergunta: PunicaoEntity)

    @Update
    suspend fun update(pergunta: PunicaoEntity)

    @Delete
    suspend fun delete(pergunta: PunicaoEntity)

    @Query("DELETE FROM perguntas WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM perguntas")
    suspend fun deleteAll()
}
