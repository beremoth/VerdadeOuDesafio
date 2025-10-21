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
    version = 8, 
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

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "verdade_ou_desafio_db"
                )
                    .fallbackToDestructiveMigration()
                    // Passa o 'scope' para o Callback
                    .addCallback(DatabaseCallback(context, scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // O Callback agora é uma classe interna que tem acesso ao INSTANCE
        private class DatabaseCallback(
            private val context: Context,
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d("AppDatabase", "onCreate triggered. Lançando coroutine...")
                
                // Lança a corrotina
                scope.launch(Dispatchers.IO) {

                    val database = INSTANCE ?: getDatabase(context, scope)
                    
                    // Cria o inicializador e chama as funções
                    val initializer = DatabaseInitializer(context)
                    
                    Log.d("AppDatabase", "Populando JSON...")
                    initializer.initializeJsonData(database)
                    
                    Log.d("AppDatabase", "Populando Raspadinhas...")
                    initializer.initializeRaspadinhas(database)
                }
            }
        }
    }
}