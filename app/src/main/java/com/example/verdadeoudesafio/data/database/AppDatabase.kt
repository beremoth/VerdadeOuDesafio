package com.example.verdadeoudesafio.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.verdadeoudesafio.data.dao.*
import com.example.verdadeoudesafio.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PerguntaEntity::class,
        DesafioEntity::class,
        PunicaoEntity::class,
        RaspadinhaEntity::class
    ],
    version = 11,
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "verdade_ou_desafio_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                // ✅ Verifica versão do JSON e atualiza se necessário
                CoroutineScope(Dispatchers.IO).launch {
                    val initializer = DatabaseInitializer(context)
                    initializer.checkAndReloadIfNewVersion(instance)
                }

                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Popula apenas na CRIAÇÃO do banco (primeira vez)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = AppDatabase.getDatabase(context)
                    val initializer = DatabaseInitializer(context)
                    initializer.initializeJsonData(database)
                    initializer.initializeRaspadinhas(database)
                    Log.d("AppDatabase", "Banco populado pela primeira vez.")
                }
            }
        }
    }
}