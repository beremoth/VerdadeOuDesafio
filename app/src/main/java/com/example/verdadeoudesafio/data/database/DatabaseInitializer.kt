package com.example.verdadeoudesafio.data.database

import android.content.Context
import android.util.Log
import com.example.verdadeoudesafio.data.entity.DesafioEntity
import com.example.verdadeoudesafio.data.entity.PerguntaEntity
import com.example.verdadeoudesafio.data.entity.PunicaoEntity
import com.example.verdadeoudesafio.data.entity.RaspadinhaEntity
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class DatabaseInitializer(private val context: Context) {

    // A função getCallback() foi removida daqui

    // Função para ler o JSON do ASSETS
    private fun loadJSON(fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e("DatabaseInitializer", "Erro ao ler $fileName de assets", e)
            ""
        }
    }

    // Esta função agora é 'public' (padrão) para o AppDatabase chamar
    suspend fun initializeJsonData(database: AppDatabase) {
        try {
            val jsonString = loadJSON("truth_or_dare.json")
            if (jsonString.isEmpty()) {
                Log.e("DatabaseInitializer", "truth_or_dare.json está vazio ou não foi encontrado em assets.")
                return
            }

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
                            perguntaDao.insert(PerguntaEntity(texto = text, level = level))
                        }
                        "dare" -> {
                            desafioDao.insert(DesafioEntity(texto = text, level = level, tempo = duration))
                        }
                    }
                }
                Log.d("DatabaseInitializer", "Perguntas/Desafios carregados de assets.")
            } else {
                Log.d("DatabaseInitializer", "JSON não tem 'questions' ou perguntas/desafios já populados.")
            }

            // Popula Punições (punishments)
            if (root.has("punishments") && punicaoDao.count() == 0) {
                val punishmentsArray = root.getJSONArray("punishments")
                for (i in 0 until punishmentsArray.length()) {
                    val item = punishmentsArray.getJSONObject(i)
                    val text = item.optString("text", "")
                    val level = item.optInt("level", 1)

                    punicaoDao.insert(PunicaoEntity(texto = text, level = level))
                }
                Log.d("DatabaseInitializer", "Punições carregadas de assets.")
            } else {
                Log.d("DatabaseInitializer", "JSON não tem 'punishments' ou punições já populadas.")
            }

        } catch (e: Exception) {
            Log.e("DatabaseInitializer", "Erro CRÍTICO ao popular dados do JSON de assets", e)
        }
    }

    // Esta função agora é 'public' (padrão)
    suspend fun initializeRaspadinhas(database: AppDatabase) {
        val raspadinhaDao = database.raspadinhaDao()
        if (raspadinhaDao.count() > 0) {
            Log.d("DatabaseInitializer", "Raspadinhas já existem no DB. Pulando.")
            return
        }

        Log.d("DatabaseInitializer", "Inicializando imagens de Raspadinha de assets/raspadinhas...")

        val imageFolderName = "raspadinhas"
        val assets = context.assets

        val destinationDirectory = File(context.filesDir, "raspadinhas")
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs()
        }

        try {
            val imageFileNames = assets.list(imageFolderName) ?: arrayOf()
            if (imageFileNames.isEmpty()) {
                Log.w("DatabaseInitializer", "Nenhuma imagem encontrada em assets/raspadinhas")
                return
            }

            for (fileName in imageFileNames) {
                val destinationFile = File(destinationDirectory, fileName)
                val inputStream: InputStream = assets.open("$imageFolderName/$fileName")
                val outputStream = FileOutputStream(destinationFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()

                val newRaspadinha = RaspadinhaEntity(imagePath = destinationFile.absolutePath)
                raspadinhaDao.insert(newRaspadinha)
                Log.d("DatabaseInitializer", "Copiou de assets e salvou no DB: ${destinationFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e("DatabaseInitializer", "Erro ao inicializar raspadinhas de assets", e)
        }
    }
}