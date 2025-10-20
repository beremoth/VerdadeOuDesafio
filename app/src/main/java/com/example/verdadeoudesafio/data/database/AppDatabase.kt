package com.example.verdadeoudesafio.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.verdadeoudesafio.data.dao.*
import com.example.verdadeoudesafio.data.entity.*

@Database(
    entities = [
        PerguntaEntity::class,
        DesafioEntity::class,
        PunicaoEntity::class,
        RaspadinhaEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun perguntaDao(): PerguntaDao
    abstract fun desafioDao(): DesafioDao
    abstract fun punicaoDao(): PunicaoDao
    abstract fun raspadinhaDao(): RaspadinhaDao
}
