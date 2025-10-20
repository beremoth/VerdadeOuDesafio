package com.example.verdadeoudesafio.data.database

import android.content.Context
import com.example.verdadeoudesafio.data.AppDatabase
import com.example.verdadeoudesafio.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object DatabaseInitializer {

    suspend fun populateDatabaseIfEmpty(context: Context, db: AppDatabase) {
        withContext(Dispatchers.IO) {
            val perguntasVazias = db.perguntaDao().getAll().isEmpty()
            val desafiosVazios = db.desafioDao().getAll().isEmpty()
            val punicoesVazias = db.punicaoDao().getAll().isEmpty()

            val jsonString = loadJSON(context, "truth_or_dare.json")
            val root = JSONObject(jsonString)

            // ============ PERGUNTAS E DESAFIOS ============
            val questionsArray = root.getJSONArray("questions")
            for (i in 0 until questionsArray.length()) {
                val item = questionsArray.getJSONObject(i)
                val type = item.optString("type", "")
                val text = item.optString("text", "")
                val level = item.optInt("level", 1)
                val duration = item.optInt("duration", 0)

                when (type.lowercase()) {
                    "truth" -> {
                        if (perguntasVazias)
                            db.perguntaDao().insert(PerguntaEntity(texto = text, level = level))
                    }
                    "dare" -> {
                        if (desafiosVazios)
                            db.desafioDao().insert(DesafioEntity(texto = text, level = level, tempo = duration))
                    }
                }
            }

            // ============ PUNIÇÕES ============
            if (root.has("punishments") && punicoesVazias) {
                val punishmentsArray = root.getJSONArray("punishments")
                for (i in 0 until punishmentsArray.length()) {
                    val item = punishmentsArray.getJSONObject(i)
                    val text = item.optString("text", "")
                    val level = item.optInt("level", 1)
                    db.punicaoDao().insert(PunicaoEntity(texto = text, level = level))
                }
            }
        }
    }

    private fun loadJSON(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }
}
