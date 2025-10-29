package com.example.verdadeoudesafio.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.verdadeoudesafio.data.dao.*
import com.example.verdadeoudesafio.data.entity.*
import kotlinx.coroutines.*

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

        /**
         * Retorna a instância única do banco.
         * Se o banco ainda não existe fisicamente, cria e popula automaticamente.
         */
        fun getDatabase(context: Context, scope: CoroutineScope? = null): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val coroutineScope = scope ?: CoroutineScope(Dispatchers.IO)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "verdade_ou_desafio_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context, coroutineScope))
                    .build()

                INSTANCE = instance

                // 🔹 Fallback extra: se o banco estiver vazio, popula mesmo sem onCreate
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        instance.openHelper.writableDatabase // Garante criação física
                        val perguntaCount = instance.perguntaDao().count()
                        val desafioCount = instance.desafioDao().count()
                        val punicaoCount = instance.punicaoDao().count()
                        val raspadinhaCount = instance.raspadinhaDao().count()

                        Log.d("AppDatabase", "Verificando conteúdo: Perguntas=$perguntaCount, Desafios=$desafioCount, Punições=$punicaoCount, Raspadinhas=$raspadinhaCount")

                        if (perguntaCount == 0 && desafioCount == 0 && punicaoCount == 0 && raspadinhaCount == 0) {
                            Log.w("AppDatabase", "Banco detectado vazio. Forçando inicialização...")
                            val initializer = DatabaseInitializer(context)
                            initializer.checkAndUpdateJsonData(instance)
                            initializer.checkAndUpdateRaspadinhas(instance)
                            Log.d("AppDatabase", "Banco populado via fallback automático.")
                        } else {
                            Log.d("AppDatabase", "Banco já contém dados. Nenhuma ação necessária.")
                        }
                    } catch (e: Exception) {
                        Log.e("AppDatabase", "Erro ao verificar/popular banco: ${e.message}", e)
                    }
                }

                instance
            }
        }

        /**
         * Callback executado apenas quando o banco é criado pela primeira vez.
         * Ideal para popular dados iniciais.
         */
        private class DatabaseCallback(
            private val context: Context,
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d("AppDatabase", "onCreate() chamado — inicializando banco...")

                scope.launch(Dispatchers.IO) {
                    val database = INSTANCE ?: return@launch
                    try {
                        val initializer = DatabaseInitializer(context)
                        Log.d("AppDatabase", "→ Populando JSON inicial...")
                        initializer.checkAndUpdateJsonData(database)

                        Log.d("AppDatabase", "→ Populando imagens de Raspadinhas...")
                        initializer.checkAndUpdateRaspadinhas(database)

                        Log.d("AppDatabase", "Banco populado com sucesso via onCreate.")
                    } catch (e: Exception) {
                        Log.e("AppDatabase", "Erro ao popular banco no onCreate: ${e.message}", e)
                    }
                }
            }
        }
    }
}
