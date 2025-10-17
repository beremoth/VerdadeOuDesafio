package com.example.verdadeoudesafio.game

import android.content.Context
import com.example.verdadeoudesafio.data.DataRepository
import com.example.verdadeoudesafio.data.entity.PerguntaEntity
import com.example.verdadeoudesafio.data.entity.DesafioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class GameManager(private val context: Context) {

    private val repository = DataRepository.getInstance(context)

    suspend fun getRandomPergunta(): PerguntaEntity? = withContext(Dispatchers.IO) {
        val perguntas = repository.getPerguntas()
        if (perguntas.isNotEmpty()) perguntas.random() else null
    }

    suspend fun getRandomDesafio(): DesafioEntity? = withContext(Dispatchers.IO) {
        val desafios = repository.getDesafios()
        if (desafios.isNotEmpty()) desafios.random() else null
    }

    suspend fun getRandomPerguntaOuDesafio(): String = withContext(Dispatchers.IO) {
        if (Random.nextBoolean()) {
            getRandomPergunta()?.texto ?: "Sem perguntas disponíveis"
        } else {
            getRandomDesafio()?.texto ?: "Sem desafios disponíveis"
        }
    }
}
