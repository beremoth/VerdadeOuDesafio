package com.example.verdadeoudesafio.data.database

import android.content.Context
import com.example.verdadeoudesafio.data.AppDatabase
import com.example.verdadeoudesafio.data.entity.DesafioEntity
import com.example.verdadeoudesafio.data.entity.PerguntaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

object DatabaseInitializer {

    suspend fun populateDatabaseIfEmpty(context: Context, db: AppDatabase) {
        withContext(Dispatchers.IO) {
            if (db.perguntaDao().getAll().isEmpty()) {
                val perguntasJson = loadJSON(context, "perguntas.json")
                val perguntas = JSONArray(perguntasJson)
                for (i in 0 until perguntas.length()) {
                    val texto = perguntas.getString(i)
                    db.perguntaDao().insert(PerguntaEntity(texto = texto))
                }
            }

            if (db.desafioDao().getAll().isEmpty()) {
                val desafiosJson = loadJSON(context, "desafios.json")
                val desafios = JSONArray(desafiosJson)
                for (i in 0 until desafios.length()) {
                    val texto = desafios.getString(i)
                    val tempo = desafios.getInt(i)
                    db.desafioDao().insert(DesafioEntity(texto = texto, tempo = tempo))
                }
            }
        }
    }

    private fun loadJSON(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }
}
