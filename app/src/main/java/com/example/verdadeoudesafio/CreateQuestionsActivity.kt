package com.example.verdadeoudesafio

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class CreateQuestionsActivity : AppCompatActivity() {

    private lateinit var truthQuestionInput: EditText
    private lateinit var dareQuestionInput: EditText
    private lateinit var levelInput: EditText
    private lateinit var saveQuestionsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_questions)

        truthQuestionInput = findViewById(R.id.truthQuestionInput)
        dareQuestionInput = findViewById(R.id.dareQuestionInput)
        levelInput = findViewById(R.id.levelInput)
        saveQuestionsButton = findViewById(R.id.saveQuestionsButton)

        saveQuestionsButton.setOnClickListener {
            val truthQuestion = truthQuestionInput.text.toString().trim()
            val dareQuestion = dareQuestionInput.text.toString().trim()
            val level = levelInput.text.toString().trim()

            if (truthQuestion.isNotEmpty() && dareQuestion.isNotEmpty() && level.isNotEmpty()) {
                val levelInt = level.toIntOrNull()
                if (levelInt != null && levelInt in 1..3) {
                    saveQuestionsToJson(truthQuestion, dareQuestion, levelInt)
                    Toast.makeText(this, "Perguntas salvas com sucesso!", Toast.LENGTH_SHORT).show()
                    finish() // Fecha a Activity após salvar
                } else {
                    Toast.makeText(this, "O nível deve ser um número entre 1 e 3.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveQuestionsToJson(truthQuestion: String, dareQuestion: String, level: Int) {
        try {
            // Ler o arquivo JSON existente
            val inputStream = resources.openRawResource(R.raw.truth_or_dare)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            // Criar novos objetos JSON para as perguntas
            val truthObject = JSONObject().apply {
                put("text", truthQuestion)
                put("type", "truth")
                put("level", level)
            }

            val dareObject = JSONObject().apply {
                put("text", dareQuestion)
                put("type", "dare")
                put("level", level)
            }

            // Adicionar as novas perguntas ao array
            jsonArray.put(truthObject)
            jsonArray.put(dareObject)

            // Salvar o JSON atualizado no arquivo
            val outputStream = openFileOutput("truth_or_dare.json", Context.MODE_PRIVATE)
            OutputStreamWriter(outputStream).use { writer ->
                writer.write(jsonArray.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar perguntas.", Toast.LENGTH_SHORT).show()
        }
    }
}