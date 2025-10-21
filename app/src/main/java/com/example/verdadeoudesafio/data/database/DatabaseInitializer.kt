package com.example.verdadeoudesafio.data.database

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.verdadeoudesafio.data.entity.DesafioEntity
import com.example.verdadeoudesafio.data.entity.PerguntaEntity
import com.example.verdadeoudesafio.data.entity.PunicaoEntity
import com.example.verdadeoudesafio.data.entity.RaspadinhaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class DatabaseInitializer(private val context: Context) {

    fun getCallback(scope: CoroutineScope): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d("DatabaseInitializer", "Banco de dados CRIADO, populando dados do ASSETS...")
                scope.launch(Dispatchers.IO) {
                    val database = AppDatabase.getDatabase(context, this)
                    initializeJsonData(database)
                    initializeRaspadinhas(database) // Chama a nova função
                }
            }
        }
    }

    // Função para ler o JSON do ASSETS
    private fun loadJSON(fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e("DatabaseInitializer", "Erro ao ler $fileName de assets", e)
            ""
        }
    }

    // Popula o JSON (usando a lógica do seu arquivo)
    private suspend fun initializeJsonData(database: AppDatabase) {
        try {
            val jsonString = loadJSON("truth_or_dare.json")
            if (jsonString.isEmpty()) return

            val root = JSONObject(jsonString)
            val perguntaDao = database.perguntaDao()
            val desafioDao = database.desafioDao()
            val punicaoDao = database.punicaoDao()

            // Popula Perguntas e Desafios (questions)
            if (root.has("questions") && (perguntaDao.count() == 0 || desafioDao.count() == 0)) {
                val questionsArray = root.getJSONArray("questions")
                for (i in 0 until questionsArray.length()) {
                    val item = questionsArray.getJSONObject(i)
                    val type = item.optString("type", "")
                    val text = item.optString("text", "")
                    val level = item.optInt("level", 1)
                    val duration = item.optInt("duration", 0)

                    when (type.lowercase()) {
                        "truth" -> {
                            // IMPORTANTE: verifique se o nome do campo é "text" ou "texto"
                            perguntaDao.insert(PerguntaEntity(text = text, level = level))
                        }
                        "dare" -> {
                            // IMPORTANTE: verifique se o nome do campo é "time" ou "tempo"
                            desafioDao.insert(DesafioEntity(text = text, level = level, time = duration))
                        }
                    }
                }
                Log.d("DatabaseInitializer", "Perguntas/Desafios carregados de assets.")
            }

            // Popula Punições (punishments)
            if (root.has("punishments") && punicaoDao.count() == 0) {
                val punishmentsArray = root.getJSONArray("punishments")
                for (i in 0 until punishmentsArray.length()) {
                    val item = punishmentsArray.getJSONObject(i)
                    val text = item.optString("text", "")
                    val level = item.optInt("level", 1)
                    // IMPORTANTE: verifique se o nome do campo é "text" ou "texto"
                    punicaoDao.insert(PunicaoEntity(text = text, level = level))
                }
                Log.d("DatabaseInitializer", "Punições carregadas de assets.")
            }

        } catch (e: Exception) {
            Log.e("DatabaseInitializer", "Erro ao popular dados do JSON de assets", e)
        }
    }

    // ESTA É A FUNÇÃO QUE MAPAIA E COPIA AS IMAGENS DO ASSETS
    private suspend fun initializeRaspadinhas(database: AppDatabase) {
        val raspadinhaDao = database.raspadinhaDao()
        if (raspadinhaDao.count() > 0) {
            Log.d("DatabaseInitializer", "Raspadinhas já existem no DB. Pulando.")
            return
        }

        Log.d("DatabaseInitializer", "Inicializando imagens de Raspadinha de assets/raspadinhas...")

        val imageFolderName = "raspadinhas" // Nome da pasta em assets
        val assets = context.assets

        // 1. Onde vamos salvar as imagens (armazenamento interno do app)
        val destinationDirectory = File(context.filesDir, "raspadinhas")
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs()
        }

        try {
            // 2. Lista todos os arquivos dentro de 'assets/raspadinhas'
            val imageFileNames = assets.list(imageFolderName) ?: arrayOf()
            if (imageFileNames.isEmpty()) {
                Log.w("DatabaseInitializer", "Nenhuma imagem encontrada em assets/raspadinhas")
                return
            }

            for (fileName in imageFileNames) {
                val destinationFile = File(destinationDirectory, fileName)

                // 3. Abre o arquivo de assets
                val inputStream: InputStream = assets.open("$imageFolderName/$fileName")

                // 4. Copia o arquivo para o armazenamento interno
                val outputStream = FileOutputStream(destinationFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()

                // 5. Salva o NOVO caminho (do armaz. interno) no banco de dados
                val newRaspadinha = RaspadinhaEntity(imagePath = destinationFile.absolutePath)
                raspadinhaDao.insert(newRaspadinha)
                Log.d("DatabaseInitializer", "Copiou de assets e salvou no DB: ${destinationFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e("DatabaseInitializer", "Erro ao inicializar raspadinhas de assets", e)
        }
    }
}