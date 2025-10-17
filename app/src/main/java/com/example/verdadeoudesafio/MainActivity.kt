package com.example.verdadeoudesafio

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.data.database.DatabaseInitializer
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    // Variáveis globais
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var questionText: TextView
    private lateinit var playerText : TextView
    private lateinit var truthButton: Button
    private lateinit var dareButton: Button
    private lateinit var skipButton: Button
    private lateinit var settingsButton: Button
    private lateinit var levelText: TextView
    private lateinit var timerText: TextView
    private lateinit var startTimerButton: Button
    private lateinit var players: MutableList<String>
    private var currentPlayerIndex = 0
    private var currentLevel = 2
    private var availableConsequences: MutableList<String> = mutableListOf()
    private var availableTruthQuestions: MutableList<DatabaseHelper.Question> = mutableListOf()
    private var availableDareQuestions: MutableList<DatabaseHelper.Question> = mutableListOf()
    private var currentTimer: CountDownTimer? = null
    private var currentQuestionDuration: Int? = null
    private var isGameStarted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = AppDatabase.getDatabase(this)

        // Popula o banco apenas se estiver vazio
        lifecycleScope.launch {
            DatabaseInitializer.populateDatabaseIfEmpty(this@MainActivity, db)
        }


        // Verifica se a permissão já foi concedida
        if (!checkVibrationPermission()) {
            requestVibrationPermission()
        }


        // Inicializa os componentes
        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        playerText = findViewById(R.id.player_text)
        questionText = findViewById(R.id.question_text)
        truthButton = findViewById(R.id.btn_truth)
        dareButton = findViewById(R.id.btn_dare)
        skipButton = findViewById(R.id.btn_consequence)
        settingsButton = findViewById(R.id.btn_settings)
        levelText = findViewById(R.id.level_text)
        timerText = findViewById(R.id.timer_text)
        startTimerButton = findViewById(R.id.btn_start_timer)

        // Carrega os jogadores e sorteia a ordem
        loadPlayers()
        updatePlayerText() // Exibe a lista completa antes de iniciar o jogo

        // Carrega o nível salvo
        currentLevel = sharedPreferences.getInt("selected_level", 2)
        Log.d("MainActivity", "Nível carregado: $currentLevel")
        updateLevelText()

        // Configuração dos botões
        truthButton.setOnClickListener {
            isGameStarted = true  // Marca que o jogo começou
            currentTimer?.cancel()
            showQuestion("truth", currentLevel)
            nextPlayer()
        }

        dareButton.setOnClickListener {
            isGameStarted = true
            currentTimer?.cancel()
            showQuestion("dare", currentLevel)
            nextPlayer()
        }

        skipButton.setOnClickListener {
            isGameStarted = true
            currentTimer?.cancel()
            showConsequence()
            nextPlayer()
        }



        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        startTimerButton.setOnClickListener {
            currentQuestionDuration?.let { duration ->
                startTimer(duration)
            }
        }

        startTimerButton.visibility = View.GONE
    }

    private fun loadPlayers() {
        val savedPlayers = sharedPreferences.getStringSet("players_list", emptySet())?.toList() ?: emptyList()
        players = if (savedPlayers.isNotEmpty()) savedPlayers.shuffled().toMutableList() else mutableListOf("Jogador 1")
        currentPlayerIndex = 0
    }

    private fun updatePlayerText() {
        if (!isGameStarted) { // Exibe a lista apenas antes do primeiro turno
            val playersListText = players.joinToString("\n") { "- $it" }
            playerText.text = "Ordem dos jogadores:\n$playersListText\nPrimeiro a jogar: ${players[0]}"
        } else { // Durante o jogo, mostra apenas o jogador atual
            val currentPlayer = players[currentPlayerIndex]
            playerText.text = "Proximo é: $currentPlayer"
        }
    }



    private fun nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        updatePlayerText()
    }

    private fun startTimer(durationInSeconds: Int) {
        currentTimer?.cancel()

        val totalTimeInMillis = durationInSeconds * 1000L
        currentTimer = object : CountDownTimer(totalTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                val minutes = secondsRemaining / 60
                val seconds = secondsRemaining % 60
                timerText.text = String.format("%02d:%02d", minutes, seconds)

                timerText.isActivated = secondsRemaining <= 5
            }

            override fun onFinish() {
                timerText.text = "Tempo esgotado!"
                timerText.isActivated = true
                startTimerButton.visibility = View.VISIBLE

                vibratePhone() // para vibração do celular
            }
        }.start()

        timerText.isActivated = false
        startTimerButton.visibility = View.GONE
    }

    /**
     * Carrega as perguntas disponíveis para o tipo e nível especificados.
     */
    private suspend fun loadAvailableQuestions(level: Int, db: AppDatabase) {
        val allPerguntas = db.perguntaDao().getAll()
        val allDesafios = db.desafioDao().getAll()

        availableTruthQuestions.clear()
        availableDareQuestions.clear()

        // Converter PerguntaEntity e DesafioEntity para a estrutura usada no jogo
        availableTruthQuestions.addAll(allPerguntas.map { DatabaseHelper.Question(it.texto, "truth", level, null) })
        availableDareQuestions.addAll(allDesafios.map { DatabaseHelper.Question(it.texto, "dare", level, null) })

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
        lifecycleScope.launch {
            if (availableTruthQuestions.isEmpty() || availableDareQuestions.isEmpty()) {
                loadAvailableQuestions(level, db)
            }
        }

        val question = getRandomQuestion(type)

        if (question != null) {
            questionText.text = question.text

            // Verifica se há um tempo associado à pergunta
            currentQuestionDuration = question.duration
            if (currentQuestionDuration != null && currentQuestionDuration!! > 0) {
                timerText.text = String.format("%02d:%02d", currentQuestionDuration!! / 60, currentQuestionDuration!! % 60)
                timerText.visibility = View.VISIBLE
                startTimerButton.visibility = View.VISIBLE
            } else {
                timerText.visibility = View.GONE
                startTimerButton.visibility = View.GONE
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
     * Atualiza o texto do nível
     */
    private fun updateLevelText() {
        levelText.text = when (currentLevel) {
            1 -> "Nível: Leve"
            2 -> "Nível: Moderado"
            3 -> "Nível: Extremo"
            else -> "Nível: Desconhecido"
        }
    }

    private fun vibratePhone() {
        if (!checkVibrationPermission()) {
            Log.e("Vibration", "Permissão de vibração negada")
            return
        }

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }


    private fun checkVibrationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Versões abaixo do Android 12 não precisam de permissão extra
        }
    }

    private fun requestVibrationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.VIBRATE), 100)
        }
    }
}

