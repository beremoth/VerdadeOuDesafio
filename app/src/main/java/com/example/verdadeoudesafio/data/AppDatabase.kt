package com.example.verdadeoudesafio.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.verdadeoudesafio.data.dao.PerguntaDao
import com.example.verdadeoudesafio.data.entity.PerguntaEntity

@Database(
    entities = [PerguntaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun perguntaDao(): PerguntaDao
}
