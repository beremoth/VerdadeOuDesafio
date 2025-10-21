package com.example.verdadeoudesafio

import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.data.entity.DesafioEntity
import com.example.verdadeoudesafio.data.entity.PerguntaEntity
import com.example.verdadeoudesafio.data.entity.PunicaoEntity
import com.example.verdadeoudesafio.data.entity.RaspadinhaEntity

class GameManager( db: AppDatabase) {

    private val perguntaDao = db.perguntaDao()
    private val desafioDao = db.desafioDao()
    private val punicaoDao = db.punicaoDao()
    private val raspadinhaDao = db.raspadinhaDao()

    suspend fun getRandomPergunta(level: Int): PerguntaEntity? {
        // Agora chama a função correta
        return perguntaDao.getRandomByLevel(level)
    }

    suspend fun getRandomDesafio(level: Int): DesafioEntity? {
        // Agora chama a função correta
        return desafioDao.getRandomByLevel(level)
    }

    suspend fun getRandomPunicao(level: Int): PunicaoEntity? {
        // Agora chama a função correta
        return punicaoDao.getRandomByLevel(level)
    }

    suspend fun getRandomRaspadinha(): RaspadinhaEntity? {
        return raspadinhaDao.getRandom()
    }
}