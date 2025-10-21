package com.example.verdadeoudesafio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.data.database.DatabaseInitializer
import com.example.verdadeoudesafio.game.GameManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var questionText: TextView
    private lateinit var playerText: TextView
    private lateinit var truthButton: Button
    private lateinit var dareButton: Button
    private lateinit var skipButton: Button
    private lateinit var settingsButton: Button
    private lateinit var levelText: TextView
    private lateinit var timerText: TextView
    private lateinit var startTimerButton: Button
    private lateinit var db: AppDatabase
    private val gameManager by lazy { GameManager(this) }

    private var players: MutableList<String> = mutableListOf()
    private var currentPlayerIndex = 0
    private var currentLevel = 2
    private var currentTimer: CountDownTimer? = null
    private var currentQuestionDuration: Int = 0
    private var isGameStarted = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "verdade_ou_desafio_db"
        ).build()

        lifecycleScope.launch {
            DatabaseInitializer.populateDatabaseIfEmpty(this@MainActivity, db)
        }

        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        initializeUI()
        requestVibrationPermissionIfNeeded()
        loadPlayers()
        updatePlayerText()
        loadLevel()


        truthButton.setOnClickListener {
            isGameStarted = true
            currentTimer?.cancel()
            showQuestion("truth")
            nextPlayer()
        }

        dareButton.setOnClickListener {
            isGameStarted = true
            currentTimer?.cancel()
            showQuestion("dare")
            nextPlayer()
        }

        skipButton.setOnClickListener {
            isGameStarted = true
            currentTimer?.cancel()
            showPunishment()
            nextPlayer()
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        startTimerButton.setOnClickListener {
            if (currentQuestionDuration > 0) {
                startTimer(currentQuestionDuration)
            }
        }

        startTimerButton.visibility = View.GONE
    }


    private fun initializeUI() {
        questionText = findViewById(R.id.question_text)
        playerText = findViewById(R.id.player_text)
        truthButton = findViewById(R.id.btn_truth)
        dareButton = findViewById(R.id.btn_dare)
        skipButton = findViewById(R.id.btn_consequence)
        settingsButton = findViewById(R.id.btn_settings)
        levelText = findViewById(R.id.level_text)
        timerText = findViewById(R.id.timer_text)
        startTimerButton = findViewById(R.id.btn_start_timer)
    }

    private fun loadPlayers() {
        val saved = sharedPreferences.getStringSet("players_list", emptySet())?.toList() ?: emptyList()
        players = if (saved.isNotEmpty()) saved.shuffled().toMutableList() else mutableListOf("Jogador 1")
        currentPlayerIndex = 0
    }

    private fun updatePlayerText() {
        if (!isGameStarted) {
            val list = players.joinToString("\n") { "- $it" }
            playerText.text = "Ordem dos jogadores:\n$list\nPrimeiro a jogar: ${players[0]}"
        } else {
            val current = players[currentPlayerIndex]
            playerText.text = "Próximo é: $current"
        }
    }

    private fun nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        updatePlayerText()
    }

    private fun loadLevel() {
        currentLevel = sharedPreferences.getInt("selected_level", 2)
    }

    private fun showQuestion(type: String) {
        lifecycleScope.launch {
            // Limpa o timer (isso está correto)
            currentTimer?.cancel()
            timerText.visibility = View.GONE
            startTimerButton.visibility = View.GONE

            val questionTextValue = when (type) {
                "truth" -> {
                    // Lógica antiga removida
                    val pergunta = gameManager.getRandomPergunta()
                    pergunta?.texto ?: "Nenhuma pergunta de verdade disponível!"
                }

                "dare" -> {
                    // Lógica antiga removida
                    val desafio = gameManager.getRandomDesafio()
                    if (desafio != null) {
                        currentQuestionDuration = desafio.tempo

                        if (desafio.tempo > 0) {
                            val min = desafio.tempo / 60
                            val sec = desafio.tempo % 60
                            timerText.text = String.format("%02d:%02d", min, sec)
                            timerText.visibility = View.VISIBLE
                            startTimerButton.visibility = View.VISIBLE
                        }

                        desafio.texto
                    } else {
                        "Nenhum desafio disponível!"
                    }
                }

                else -> "Tipo inválido!"
            }

            questionText.text = questionTextValue
        }
    }

    private fun showPunishment() {
        lifecycleScope.launch {
            val punicao = gameManager.getRandomPunicao(currentLevel)
            if (punicao != null) {
                questionText.text = "PUNIÇÃO:\n${punicao.texto}"
            } else {
                questionText.text = "Sem punições disponíveis!"
            }
        }
    }


    private fun startTimer(duration: Int) {
        currentTimer?.cancel()
        currentTimer = object : CountDownTimer(duration * 1000L, 1000) {
            override fun onTick(millis: Long) {
                val sec = millis / 1000
                val min = sec / 60
                val rem = sec % 60
                timerText.text = String.format("%02d:%02d", min, rem)
            }

            override fun onFinish() {
                timerText.text = "Tempo esgotado!"
                vibratePhone()
                startTimerButton.visibility = View.VISIBLE
            }
        }.start()
        timerText.visibility = View.VISIBLE
        startTimerButton.visibility = View.GONE
    }

    private fun vibratePhone() {
        if (!checkVibrationPermission()) return

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
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
        return ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestVibrationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !checkVibrationPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.VIBRATE), 100)
        }
    }
}
