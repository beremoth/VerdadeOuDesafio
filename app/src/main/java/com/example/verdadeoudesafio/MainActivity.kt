package com.example.verdadeoudesafio

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    // Variáveis globais
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var questionText: TextView
    private lateinit var truthButton: Button
    private lateinit var dareButton: Button
    private lateinit var skipButton: Button
    private lateinit var settingsButton: Button
    private lateinit var levelText: TextView
    private lateinit var timerText: TextView
    private var currentLevel = 2 // Default: Moderado (2)
    private var availableQuestions: MutableList<DatabaseHelper.Question> = mutableListOf()
    private var availableConsequences: MutableList<String> = mutableListOf()
    private var availableTruthQuestions: MutableList<DatabaseHelper.Question> = mutableListOf()
    private var availableDareQuestions: MutableList<DatabaseHelper.Question> = mutableListOf()
    private var currentTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa o banco de dados
        dbHelper = DatabaseHelper(this)

        // Inicializa o SharedPreferences
        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Vincula os componentes da interface
        questionText = findViewById(R.id.questionText)
        truthButton = findViewById(R.id.truthButton)
        dareButton = findViewById(R.id.dareButton)
        skipButton = findViewById(R.id.skipButton)
        settingsButton = findViewById(R.id.settingsButton)
        levelText = findViewById(R.id.levelText)
        timerText = findViewById(R.id.timerText)

        // Carrega o nível salvo
        currentLevel = sharedPreferences.getInt("selected_level", 2) // Padrão: Moderado (2)
        Log.d("MainActivity", "Nível carregado: $currentLevel")

        // Atualiza o texto do nível
        updateLevelText()

        // Verifica se os dados foram carregados
        if (!dbHelper.checkIfDataIsLoaded()) {
            Toast.makeText(this, "Erro ao carregar perguntas!", Toast.LENGTH_LONG).show()
            Log.e("MainActivity", "Banco de dados vazio")
            finish()
            return
        }

        // Configura os listeners
        truthButton.setOnClickListener {
            showQuestion("truth", currentLevel)
        }

        dareButton.setOnClickListener {
            showQuestion("dare", currentLevel)
        }

        skipButton.setOnClickListener {
            showConsequence()
        }

        settingsButton.setOnClickListener {
            startActivity(SettingsActivity.getIntent(this))
            currentLevel = sharedPreferences.getInt("selected_level", 2) // Padrão: Moderado (2)
            Log.d("MainActivity", "Nível carregado: $currentLevel")
            updateLevelText()
        }
    }

    /**
     * Inicia o timer.
     */
    private fun startTimer(durationInSeconds: Int) {
        // Cancela o timer ativo, se houver
        currentTimer?.cancel()

        val totalTimeInMillis = durationInSeconds * 1000L
        currentTimer = object : CountDownTimer(totalTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                val minutes = secondsRemaining / 60
                val seconds = secondsRemaining % 60
                timerText.text = String.format("%02d:%02d", minutes, seconds)

                // Altera a cor do fundo se faltarem 10 segundos ou menos
                if (secondsRemaining <= 10) {
                    timerText.setBackgroundResource(R.drawable.timer_red_background)
                } else {
                    timerText.setBackgroundResource(R.drawable.timer_background)
                }
            }

            override fun onFinish() {
                timerText.text = "Tempo esgotado!"
            }
        }.start()

        // Aplica uma animação de fade-in ao timer
        val fadeInAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in)
        timerText.startAnimation(fadeInAnimation)
    }

     /**
     * Carrega as perguntas disponíveis para o tipo e nível especificados.
     */
    private fun loadAvailableQuestions(level: Int) {
        val allQuestions = dbHelper.getAllQuestions()

        // Limpa as listas existentes
        availableTruthQuestions.clear()
        availableDareQuestions.clear()

        // Filtra as perguntas com base no tipo e no nível
        availableTruthQuestions.addAll(allQuestions.filter { it.type == "truth" && it.level == level })
        availableDareQuestions.addAll(allQuestions.filter { it.type == "dare" && it.level == level })

        // Logs para depuração
        Log.d("MainActivity", "Perguntas truth carregadas: ${availableTruthQuestions.size}")
        Log.d("MainActivity", "Perguntas dare carregadas: ${availableDareQuestions.size}")
    }

    /**
     * Obtém uma pergunta aleatória da lista disponível para o tipo especificado.
     */
    private fun getRandomQuestion(type: String): DatabaseHelper.Question? {
        return when (type) {
            "truth" -> {
                if (availableTruthQuestions.isNotEmpty()) {
                    val randomIndex = (0 until availableTruthQuestions.size).random()
                    availableTruthQuestions.removeAt(randomIndex)
                } else {
                    null
                }
            }
            "dare" -> {
                if (availableDareQuestions.isNotEmpty()) {
                    val randomIndex = (0 until availableDareQuestions.size).random()
                    availableDareQuestions.removeAt(randomIndex)
                } else {
                    null
                }
            }
            else -> null
        }
    }

    /**
     * Exibe uma pergunta aleatória com base no tipo ("verdade" ou "desafio") e no nível.
     */
    private fun showQuestion(type: String, level: Int) {
        // Carrega as perguntas disponíveis, se ainda não foram carregadas
        if (availableTruthQuestions.isEmpty() || availableDareQuestions.isEmpty()) {
            loadAvailableQuestions(level)
        }

        // Obtém uma pergunta aleatória
        val question = getRandomQuestion(type)
        if (question != null) {
            questionText.text = question.text

            // Verifica se há um tempo associado à pergunta
            if (question.duration != null && question.duration > 0) {
                timerText.visibility = View.VISIBLE
                startTimer(question.duration)
            } else {
                timerText.visibility = View.GONE
            }
        } else {
            questionText.text = "Nenhuma pergunta encontrada!"
            Toast.makeText(this, "Nenhuma pergunta deste tipo/nível", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Carrega as consequências disponíveis para o nível especificado.
     */
    private fun loadAvailableConsequences(level: Int) {
        availableConsequences.clear()
        availableConsequences.addAll(
            when (level) {
                1 -> listOf(
                    "Faça uma imitação engraçada.",
                    "Cante uma música infantil bem alto.",
                    "Fale com sotaque estrangeiro até sua próxima rodada.",
                    "Dê cinco elogios sinceros para a pessoa ao seu lado."
                )
                2 -> listOf(
                    "Tire uma peça de roupa.",
                    "Beije seu parceiro por 10 segundos.",
                    "Faça uma massagem por 1 minuto.",
                    "Mande um áudio gemendo baixinho."
                )
                3 -> listOf(
                    "Fique pelado(a) por 2 rodadas.",
                    "Deixe seu parceiro(a) usar gelo para provocar você.",
                    "Faça uma lap dance no seu parceiro(a).",
                    "Use uma venda nos olhos enquanto seu parceiro(a) te provoca."
                )
                else -> listOf("Nenhuma consequência disponível.")
            }
        )
    }

    /**
     * Obtém uma consequência aleatória da lista disponível.
     */
    private fun getRandomConsequence(): String? {
        return if (availableConsequences.isNotEmpty()) {
            val randomIndex = Random.nextInt(availableConsequences.size)
            availableConsequences.removeAt(randomIndex)
        } else {
            // Recarrega as consequências se a lista estiver vazia
            loadAvailableConsequences(currentLevel)
            availableConsequences.randomOrNull()?.also { availableConsequences.remove(it) }
        }
    }

    /**
     * Exibe uma consequência aleatória quando o jogador pula uma pergunta.
     */
    private fun showConsequence() {
        currentLevel = sharedPreferences.getInt("selected_level", 2) // Padrão: Moderado (2)
        Log.d("MainActivity", "Nível carregado: $currentLevel")

        // Carrega as consequências disponíveis, se ainda não foram carregadas
        if (availableConsequences.isEmpty()) {
            loadAvailableConsequences(currentLevel)
        }

        // Obtém uma consequência aleatória
        val consequence = getRandomConsequence()
        if (consequence != null) {
            questionText.text = "CONSEQUÊNCIA:\n$consequence"
        } else {
            questionText.text = "Nenhuma consequência disponível!"
            Toast.makeText(this, "Nenhuma consequência disponível para este nível.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Atualiza o texto do nível.
     */
    private fun updateLevelText() {
        levelText.text = when (currentLevel) {
            1 -> "Nível: Leve"
            2 -> "Nível: Moderado"
            3 -> "Nível: Extremo"
            else -> "Nível: Desconhecido"
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}