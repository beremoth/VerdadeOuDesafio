package com.example.verdadeoudesafio.data.dao

import androidx.room.*
import com.example.verdadeoudesafio.data.entity.DesafioEntity

@Dao
interface DesafioDao {
    @Query("SELECT * FROM desafios")
    suspend fun getAll(): List<DesafioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(desafio: DesafioEntity)

    @Update
    suspend fun update(desafio: DesafioEntity)

    @Delete
    suspend fun delete(desafio: DesafioEntity)

    @Query("DELETE FROM desafios")
    suspend fun deleteAll()
}
