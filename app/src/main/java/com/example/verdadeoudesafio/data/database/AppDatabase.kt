package com.example.verdadeoudesafio.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.verdadeoudesafio.data.dao.DesafioDao
import com.example.verdadeoudesafio.data.dao.PerguntaDao
import com.example.verdadeoudesafio.data.dao.PunicaoDao
import com.example.verdadeoudesafio.data.dao.RaspadinhaDao
import com.example.verdadeoudesafio.data.entity.DesafioEntity
import com.example.verdadeoudesafio.data.entity.PerguntaEntity
import com.example.verdadeoudesafio.data.entity.PunicaoEntity
import com.example.verdadeoudesafio.data.entity.RaspadinhaEntity

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