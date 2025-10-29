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

class DatabaseInitializer(private val context: Context) {

    // A função getCallback() foi removida

    private fun loadJSON(fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e("DatabaseInitializer", "Erro ao ler $fileName de assets", e)
            ""
        }
    }


    suspend fun checkAndUpdateJsonData(database: AppDatabase) {
        try {
            val jsonString = loadJSON("truth_or_dare.json")
            if (jsonString.isEmpty()) {
                Log.e("DatabaseInitializer", "truth_or_dare.json não encontrado ou vazio.")
                return
            }

            val root = JSONObject(jsonString)
            val perguntaDao = database.perguntaDao()
            val desafioDao = database.desafioDao()
            val punicaoDao = database.punicaoDao()

            // --- Perguntas e Desafios ---
            if (root.has("questions")) {
                val questionsArray = root.getJSONArray("questions")
                for (i in 0 until questionsArray.length()) {
                    val item = questionsArray.getJSONObject(i)
                    val type = item.optString("type", "").lowercase()
                    val text = item.optString("text", "").trim()
                    val level = item.optInt("level", 1)
                    val duration = item.optInt("duration", 0)

                    if (text.isEmpty()) continue

                    when (type) {
                        "truth" -> {
                            val exists = perguntaDao.existsByText(text)
                            if (!exists) {
                                perguntaDao.insert(PerguntaEntity(texto = text, level = level))
                                Log.d("DatabaseInitializer", "Nova pergunta adicionada: $text")
                            }
                        }
                        "dare" -> {
                            val exists = desafioDao.existsByText(text)
                            if (!exists) {
                                desafioDao.insert(DesafioEntity(texto = text, level = level, tempo = duration))
                                Log.d("DatabaseInitializer", "Novo desafio adicionado: $text")
                            }
                        }
                    }
                }
            }

            // --- Punições ---
            if (root.has("punishments")) {
                val punishmentsArray = root.getJSONArray("punishments")
                for (i in 0 until punishmentsArray.length()) {
                    val item = punishmentsArray.getJSONObject(i)
                    val text = item.optString("text", "").trim()
                    val level = item.optInt("level", 1)

                    if (text.isEmpty()) continue

                    val exists = punicaoDao.existsByText(text)
                    if (!exists) {
                        punicaoDao.insert(PunicaoEntity(texto = text, level = level))
                        Log.d("DatabaseInitializer", "Nova punição adicionada: $text")
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("DatabaseInitializer", "Erro ao verificar/atualizar JSON", e)
        }
    }

    suspend fun checkAndUpdateRaspadinhas(database: AppDatabase) {
        val raspadinhaDao = database.raspadinhaDao()
        val imageFolderName = "raspadinhas"
        val assets = context.assets
        val destinationDirectory = File(context.filesDir, "raspadinhas")

        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs()
        }

        try {
            val assetFileNames = assets.list(imageFolderName) ?: emptyArray()
            if (assetFileNames.isEmpty()) {
                Log.w("DatabaseInitializer", "Pasta assets/raspadinhas vazia.")
                return
            }

            // Obter nomes já salvos no banco
            val existingPaths = raspadinhaDao.getAllPaths()
            val existingFileNames = existingPaths.map { File(it).name }.toSet()

            for (fileName in assetFileNames) {
                if (fileName in existingFileNames) continue // Já existe

                // Copiar do assets para app private dir
                val destinationFile = File(destinationDirectory, fileName)
                assets.open("$imageFolderName/$fileName").use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }

                // Inserir no banco
                raspadinhaDao.insert(RaspadinhaEntity(imagePath = destinationFile.absolutePath))
                Log.d("DatabaseInitializer", "Nova raspadinha adicionada: $fileName")
            }

        } catch (e: Exception) {
            Log.e("DatabaseInitializer", "Erro ao verificar/atualizar raspadinhas", e)
        }
    }
}