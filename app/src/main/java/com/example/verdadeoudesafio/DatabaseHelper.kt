package com.example.verdadeoudesafio

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.json.JSONObject

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val mContext = context

    companion object {
        private const val DATABASE_NAME = "TruthOrDare.db"
        private const val DATABASE_VERSION = 3
        const val TABLE_QUESTIONS = "questions"
        const val COLUMN_ID = "id"
        const val COLUMN_TEXT = "text"
        const val COLUMN_TYPE = "type"  // "truth" ou "dare"
        const val COLUMN_LEVEL = "level" // 1 (leve), 2 (moderado), 3 (extremo)
        const val COLUMN_DURATION = "duration" // Novo campo opcional
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            val createTableQuery = """
                CREATE TABLE $TABLE_QUESTIONS (
                    $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_TEXT TEXT NOT NULL,
                    $COLUMN_TYPE TEXT NOT NULL,
                    $COLUMN_LEVEL INTEGER NOT NULL,
                    $COLUMN_DURATION INTEGER DEFAULT NULL -- Campo opcional
                )
            """.trimIndent()
            db.execSQL(createTableQuery)
            insertSampleQuestions(db)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erro ao criar tabela: ${e.message}")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_QUESTIONS ADD COLUMN $COLUMN_DURATION INTEGER DEFAULT NULL")
        }
    }

    private fun insertSampleQuestions(db: SQLiteDatabase) {
        try {
            val resourceId = mContext.resources.getIdentifier(
                "truth_or_dare", "raw", mContext.packageName)
            if (resourceId == 0) {
                throw Exception("Arquivo JSON não encontrado em res/assets")
            }
            val inputStream = mContext.resources.openRawResource(resourceId)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(jsonString)
            val questionsArray = json.getJSONArray("questions")

            for (i in 0 until questionsArray.length()) {
                val question = questionsArray.getJSONObject(i)
                val values = ContentValues().apply {
                    put(COLUMN_TEXT, question.getString("text"))
                    put(COLUMN_TYPE, question.getString("type"))
                    put(COLUMN_LEVEL, question.getInt("level"))
                    if (question.has("duration")) {
                        put(COLUMN_DURATION, question.getInt("duration"))
                    }
                }
                db.insert(TABLE_QUESTIONS, null, values)
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erro ao inserir perguntas: ${e.message}")
        }
    }

    data class Question(
        val text: String,
        val type: String, // Adicionado para armazenar o tipo ("truth" ou "dare")
        val level: Int,   // Adicionado para armazenar o nível 1 (leve), 2 (moderado), 3 (extremo)
        val duration: Int? // Duração é opcional
    )

    fun getQuestions(type: String, level: Int): List<Question> {
        val questions = mutableListOf<Question>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_QUESTIONS,
            arrayOf(COLUMN_TEXT, COLUMN_TYPE, COLUMN_LEVEL, COLUMN_DURATION),
            "$COLUMN_TYPE = ? AND $COLUMN_LEVEL = ?",
            arrayOf(type, level.toString()),
            null, null, null

        )
        try {
            while (cursor.moveToNext()) {
                val text = cursor.getString(0)
                val qType = cursor.getString(1)
                val qLevel = cursor.getInt(2)
                val duration = if (!cursor.isNull(3)) cursor.getInt(3) else null
                questions.add(Question(text, qType, qLevel, duration))
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erro ao buscar perguntas: ${e.message}")
        } finally {
            cursor.close()
        }
        return questions
    }

    fun getAllQuestions(): List<Question> {
        val questions = mutableListOf<Question>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_QUESTIONS,
            arrayOf(COLUMN_TEXT, COLUMN_TYPE, COLUMN_LEVEL, COLUMN_DURATION),
            null, null, null, null, null
        )
        try {
            while (cursor.moveToNext()) {
                val text = cursor.getString(0)
                val type = cursor.getString(1)
                val level = cursor.getInt(2)
                val duration = if (!cursor.isNull(3)) cursor.getInt(3) else null
                questions.add(Question(text, type, level, duration))
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erro ao buscar perguntas: ${e.message}")
        } finally {
            cursor.close()
        }
        return questions
    }

    fun checkIfDataIsLoaded(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_QUESTIONS", null)
        return try {
            cursor.moveToFirst()
            cursor.getInt(0) > 0
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erro ao verificar dados: ${e.message}")
            false
        } finally {
            cursor.close()
        }
    }
}