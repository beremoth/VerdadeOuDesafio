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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.verdadeoudesafio.data.database.AppDatabase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var questionText: TextView
    private lateinit var playerText: TextView
    private lateinit var truthButton: Button
    private lateinit var dareButton: Button
    private lateinit var skipButton: Button
    private lateinit var settingsButton: Button
    private lateinit var levelText: TextView // TextView para mostrar o nível
    private lateinit var timerText: TextView
    private lateinit var startTimerButton: Button
    private val db by lazy { AppDatabase.getDatabase(applicationContext, lifecycleScope) }
    private val gameManager by lazy { GameManager(db) }

    private var players: MutableList<String> = mutableListOf()
    private var currentPlayerIndex = 0
    private var currentLevel = 2
    private var currentTimer: CountDownTimer? = null
    private var currentQuestionDuration: Int = 0
    private var isGameStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        initializeUI() // Encontra as Views
        requestVibrationPermissionIfNeeded() // Pede permissão se necessário

        // --- Carrega dados iniciais ---
        // loadPlayers() // Movido para onResume para atualizar após Settings
        // updatePlayerText() // Movido para onResume
        // loadLevel() // Movido para onResume


        // Configuração dos botões
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
        startTimerButton.visibility = View.GONE // Esconde o botão do timer inicialmente
    }


    override fun onResume() {
        super.onResume()
        // Recarrega as configurações e atualiza a UI toda vez que a Activity volta ao foco
        loadLevel() // Carrega o nível e ATUALIZA O TEXTVIEW
        loadPlayers() // Recarrega a lista de jogadores (pode ter mudado)
        updatePlayerText() // Atualiza o texto do jogador atual/lista
    }
    // ----------------------------

    private fun initializeUI() {
        questionText = findViewById(R.id.question_text)
        playerText = findViewById(R.id.player_text)
        truthButton = findViewById(R.id.btn_truth)
        dareButton = findViewById(R.id.btn_dare)
        skipButton = findViewById(R.id.btn_consequence)
        settingsButton = findViewById(R.id.btn_settings)
        levelText = findViewById(R.id.level_text) // Encontra o TextView do nível
        timerText = findViewById(R.id.timer_text)
        startTimerButton = findViewById(R.id.btn_start_timer)
    }

    private fun loadPlayers() {
        val saved = sharedPreferences.getStringSet("players_list", emptySet())?.toList() ?: emptyList()
        players = if (saved.isNotEmpty()) saved.shuffled().toMutableList() else mutableListOf("Jogador 1")
        // Não reseta o currentPlayerIndex aqui para continuar de onde parou se voltar das config
        if (currentPlayerIndex >= players.size) { // Ajusta índice se a lista diminuiu
            currentPlayerIndex = 0
        }
    }

    private fun updatePlayerText() {
        if (players.isEmpty()) { // Segurança caso a lista fique vazia
            playerText.text = "Adicione jogadores nas Configurações"
            return
        }
        if (!isGameStarted) {
            val list = players.joinToString("\n") { "- $it" }
            playerText.text = "Ordem dos jogadores:\n$list\n\nPrimeiro a jogar: ${players[0]}"
        } else {
            // Garante que o índice é válido antes de acessar
            if (currentPlayerIndex < 0 || currentPlayerIndex >= players.size) {
                currentPlayerIndex = 0 // Reseta se inválido
            }
            val current = players[currentPlayerIndex]
            playerText.text = "É a vez de: $current" // Texto mais claro
        }
    }

    private fun nextPlayer() {
        if (players.isNotEmpty()) { // Só avança se houver jogadores
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
            updatePlayerText()
        }
    }

    private fun loadLevel() {
        currentLevel = sharedPreferences.getInt("selected_level", 2) // Pega o nível salvo (padrão 2)

        // --- ATUALIZA O TEXTVIEW DO NÍVEL ---
        levelText.text = when (currentLevel) {
            1 -> "Nível: Leve"
            2 -> "Nível: Moderado"
            3 -> "Nível: Extremo"
            else -> "Nível: Moderado (Padrão)" // Fallback
        }
        // ------------------------------------
    }

    // Busca Pergunta OU Desafio usando GameManager e o nível atual
    private fun showQuestion(type: String) {
        lifecycleScope.launch {
            currentTimer?.cancel()
            timerText.visibility = View.GONE
            startTimerButton.visibility = View.GONE

            val questionTextValue = when (type) {
                "truth" -> {
                    val pergunta = gameManager.getRandomPergunta(currentLevel)
                    pergunta?.texto ?: "Nenhuma pergunta de verdade disponível para este nível!"
                }
                "dare" -> {
                    val desafio = gameManager.getRandomDesafio(currentLevel)
                    if (desafio != null) {
                        currentQuestionDuration = desafio.tempo ?: 0
                        if (currentQuestionDuration > 0) {
                            val min = currentQuestionDuration / 60
                            val sec = currentQuestionDuration % 60
                            timerText.text = String.format("%02d:%02d", min, sec)
                            timerText.visibility = View.VISIBLE
                            startTimerButton.visibility = View.VISIBLE
                        } else {
                            timerText.visibility = View.GONE
                            startTimerButton.visibility = View.GONE
                        }
                        desafio.texto
                    } else {
                        "Nenhum desafio disponível para este nível!"
                    }
                }
                else -> "Tipo inválido!"
            }
            questionText.text = questionTextValue
        }
    }

    // Busca Punição usando GameManager e o nível atual
    private fun showPunishment() {
        lifecycleScope.launch {
            currentTimer?.cancel()
            timerText.visibility = View.GONE
            startTimerButton.visibility = View.GONE

            val punicao = gameManager.getRandomPunicao(currentLevel) // Usa o nível
            if (punicao != null) {
                questionText.text = "PUNIÇÃO:\n${punicao.texto}"
            } else {
                questionText.text = "Sem punições disponíveis para este nível!"
            }
        }
    }

    // Inicia o contador regressivo
    private fun startTimer(duration: Int) {
        currentTimer?.cancel()
        startTimerButton.visibility = View.GONE // Esconde o botão "Iniciar Timer"
        timerText.visibility = View.VISIBLE // Mostra o texto do timer

        currentTimer = object : CountDownTimer(duration * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                val minutes = secondsRemaining / 60
                val seconds = secondsRemaining % 60
                timerText.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerText.text = "Tempo esgotado!"
                vibratePhone()
                // Não mostra o botão "Iniciar" de novo automaticamente,
                // espera o próximo desafio com timer.
            }
        }.start()
    }

    // Função para vibrar o celular
    private fun vibratePhone() {
        if (!checkVibrationPermission()) return

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // Para versões mais antigas do Android
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    // Verifica se a permissão para vibrar foi concedida
    private fun checkVibrationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED
    }

    // Pede permissão para vibrar se necessário (Android 12+)
    private fun requestVibrationPermissionIfNeeded() {
        // A permissão VIBRATE só é necessária a partir do Android S (API 31) se declarada no Manifest
        // Em versões anteriores, ela é concedida automaticamente se declarada.
        // No entanto, a boa prática é verificar.
        if (!checkVibrationPermission()) {
            // Se não declarada no Manifest, não adianta pedir. Se declarada, verificar.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
                // A permissão VIBRATE foi movida para permissões normais, não precisa pedir em runtime
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Em teoria, não deveria ser necessário pedir VIBRATE em runtime, mas alguns OEMs podem exigir.
                //ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.VIBRATE), 100)
            }
        }
    }
}