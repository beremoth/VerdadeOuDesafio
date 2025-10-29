// DatabaseInitializer.kt
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

    private fun loadJSON(fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e("DatabaseInitializer", "Erro ao ler $fileName de assets", e)
            ""
        }
    }

    suspend fun initializeJsonData(database: AppDatabase) {
        // Só carrega se as tabelas estiverem vazias
        if (database.perguntaDao().count() > 0 || database.desafioDao().count() > 0) {
            Log.d("DatabaseInitializer", "Perguntas/Desafios já existem. Pulando JSON.")
            return
        }

        val jsonString = loadJSON("truth_or_dare.json")
        if (jsonString.isEmpty()) return

        val root = JSONObject(jsonString)
        val perguntaDao = database.perguntaDao()
        val desafioDao = database.desafioDao()
        val punicaoDao = database.punicaoDao()

        if (root.has("questions")) {
            val questionsArray = root.getJSONArray("questions")
            for (i in 0 until questionsArray.length()) {
                val item = questionsArray.getJSONObject(i)
                val type = item.optString("type", "").lowercase()
                val text = item.optString("text", "").trim()
                if (text.isEmpty()) continue
                val level = item.optInt("level", 1)
                val duration = item.optInt("duration", 0)

                when (type) {
                    "truth" -> perguntaDao.insert(PerguntaEntity(texto = text, level = level))
                    "dare" -> desafioDao.insert(DesafioEntity(texto = text, level = level, tempo = duration))
                }
            }
        }

        if (root.has("punishments") && punicaoDao.count() == 0) {
            val punishmentsArray = root.getJSONArray("punishments")
            for (i in 0 until punishmentsArray.length()) {
                val item = punishmentsArray.getJSONObject(i)
                val text = item.optString("text", "").trim()
                if (text.isEmpty()) continue
                val level = item.optInt("level", 1)
                punicaoDao.insert(PunicaoEntity(texto = text, level = level))
            }
        }
    }

    suspend fun initializeRaspadinhas(database: AppDatabase) {
        if (database.raspadinhaDao().count() > 0) {
            Log.d("DatabaseInitializer", "Raspadinhas já existem. Pulando.")
            return
        }

        val raspadinhaDao = database.raspadinhaDao()
        val imageFolderName = "raspadinhas"
        val assets = context.assets
        val destinationDirectory = File(context.filesDir, "raspadinhas")
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs()
        }

        try {
            val fileNames = assets.list(imageFolderName) ?: emptyArray()
            for (fileName in fileNames) {
                val destFile = File(destinationDirectory, fileName)
                assets.open("$imageFolderName/$fileName").use { input ->
                    FileOutputStream(destFile).use { output -> input.copyTo(output) }
                }
                raspadinhaDao.insert(RaspadinhaEntity(imagePath = destFile.absolutePath))
            }
        } catch (e: Exception) {
            Log.e("DatabaseInitializer", "Erro ao carregar raspadinhas", e)
        }
    }
}