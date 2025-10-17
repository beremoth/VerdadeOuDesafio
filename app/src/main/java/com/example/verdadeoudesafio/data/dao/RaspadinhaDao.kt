package com.example.verdadeoudesafio.data.dao

import androidx.room.*
import com.example.verdadeoudesafio.data.entity.RaspadinhaEntity

@Dao
interface RaspadinhaDao {

    @Query("SELECT * FROM raspadinhas ORDER BY timestamp DESC")
    suspend fun getAll(): List<RaspadinhaEntity>

    @Insert
    suspend fun insert(raspadinha: RaspadinhaEntity)

    @Update
    suspend fun update(raspadinha: RaspadinhaEntity)

    @Delete
    suspend fun delete(raspadinha: RaspadinhaEntity)

    @Query("DELETE FROM raspadinhas WHERE id = :id")
    suspend fun deleteById(id: Int)
}
