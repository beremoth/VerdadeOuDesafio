package com.example.verdadeoudesafio.data.dao

import androidx.room.*
import com.example.verdadeoudesafio.data.entity.DesafioEntity
import com.example.verdadeoudesafio.data.entity.PunicaoEntity


@Dao
interface DesafioDao {
    @Query("SELECT * FROM desafios ORDER BY timestamp DESC")
    suspend fun getAll(): List<DesafioEntity>

    @Query("SELECT * FROM desafios WHERE level = :level")
    suspend fun getByLevel(level: Int): List<DesafioEntity>

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
}
