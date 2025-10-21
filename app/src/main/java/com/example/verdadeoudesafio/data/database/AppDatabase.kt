package com.example.verdadeoudesafio.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.verdadeoudesafio.data.dao.DesafioDao
import com.example.verdadeoudesafio.data.dao.PerguntaDao
import com.example.verdadeoudesafio.data.dao.PunicaoDao
import com.example.verdadeoudesafio.data.dao.RaspadinhaDao
import com.example.verdadeoudesafio.data.entity.DesafioEntity
import com.example.verdadeoudesafio.data.entity.PerguntaEntity
import com.example.verdadeoudesafio.data.entity.PunicaoEntity
import com.example.verdadeoudesafio.data.entity.RaspadinhaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        PerguntaEntity::class,
        DesafioEntity::class,
        PunicaoEntity::class,
        RaspadinhaEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun perguntaDao(): PerguntaDao
    abstract fun desafioDao(): DesafioDao
    abstract fun punicaoDao(): PunicaoDao
    abstract fun raspadinhaDao(): RaspadinhaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val initializer = DatabaseInitializer(context.applicationContext)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "verdade_ou_desafio_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(initializer.getCallback(scope))
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}