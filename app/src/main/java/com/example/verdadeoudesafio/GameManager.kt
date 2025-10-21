package com.example.verdadeoudesafio

import com.example.verdadeoudesafio.data.database.AppDatabase
import com.example.verdadeoudesafio.data.entity.DesafioEntity
import com.example.verdadeoudesafio.data.entity.PerguntaEntity
import com.example.verdadeoudesafio.data.entity.PunicaoEntity
import com.example.verdadeoudesafio.data.entity.RaspadinhaEntity

class GameManager(db: AppDatabase) { // Removido 'private val'

    private val perguntaDao = db.perguntaDao()
    private val desafioDao = db.desafioDao()
    private val punicaoDao = db.punicaoDao()
    private val raspadinhaDao = db.raspadinhaDao()

    suspend fun getRandomPergunta(level: Int): PerguntaEntity? {
        return perguntaDao.getRandomByLevel(level)
    }

    suspend fun getRandomDesafio(level: Int): DesafioEntity? {
        return desafioDao.getRandomByLevel(level)
    }


    suspend fun getRandomPunicao(level: Int): PunicaoEntity? {
        // E precisa chamar a função correta
        return punicaoDao.getRandomByLevel(level)
    }
    // --- FIM DA CORREÇÃO ---

    suspend fun getRandomRaspadinha(): RaspadinhaEntity? {
        return raspadinhaDao.getRandom()
    }
}