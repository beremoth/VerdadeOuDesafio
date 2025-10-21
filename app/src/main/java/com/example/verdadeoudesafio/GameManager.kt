package com.example.verdadeoudesafio.game

import android.content.Context
import com.example.verdadeoudesafio.data.DataRepository
import com.example.verdadeoudesafio.data.entity.PerguntaEntity
import com.example.verdadeoudesafio.data.entity.DesafioEntity
import com.example.verdadeoudesafio.data.entity.PunicaoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class GameManager(private val context: Context) {

    private val repository = DataRepository.getInstance(context)

    suspend fun getRandomPergunta(level: Int): PerguntaEntity? = withContext(Dispatchers.IO) {
        val perguntas = repository.getPerguntasPorLevel(level)
        perguntas.randomOrNull()
    }

    suspend fun getRandomDesafio(level: Int): DesafioEntity? = withContext(Dispatchers.IO) {
        val desafios = repository.getDesafiosPorLevel(level)
        desafios.randomOrNull()
    }

    suspend fun getRandomPunicao(level: Int): PunicaoEntity? = withContext(Dispatchers.IO) {
        val punicoes = repository.getPunicoesPorLevel(level)
        punicoes.randomOrNull()
    }

    suspend fun getRandomPerguntaOuDesafio(level: Int): String = withContext(Dispatchers.IO) { // Agora recebe level
        if (Random.nextBoolean()) {
            getRandomPergunta(level)?.texto ?: "Sem perguntas disponíveis para este nível" // CORRETO
        } else {
            getRandomDesafio(level)?.texto ?: "Sem desafios disponíveis para este nível" // CORRETO
        }
    }
}
