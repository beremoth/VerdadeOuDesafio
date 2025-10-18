package com.example.verdadeoudesafio.data

import android.content.Context
import androidx.room.Room
import com.example.verdadeoudesafio.data.entity.*

class DataRepository private constructor(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "verdadeoudesafio.db"
    ).build()

    // Perguntas
    suspend fun getPerguntas(): List<PerguntaEntity> = db.perguntaDao().getAll()
    suspend fun addPergunta(texto: String) = db.perguntaDao().insert(PerguntaEntity(texto = texto))
    suspend fun updatePergunta(p: PerguntaEntity) = db.perguntaDao().update(p)
    suspend fun deletePergunta(p: PerguntaEntity) = db.perguntaDao().delete(p)

    // Desafios
    suspend fun getDesafios(): List<DesafioEntity> = db.desafioDao().getAll()
    suspend fun addDesafio(texto: String, tempo: Int) = db.desafioDao().insert(DesafioEntity(texto = texto, tempo = tempo))
    suspend fun updateDesafio(d: DesafioEntity) = db.desafioDao().update(d)
    suspend fun deleteDesafio(d: DesafioEntity) = db.desafioDao().delete(d)

    // Punições
    suspend fun getPunicoes(): List<PunicaoEntity> = db.punicaoDao().getAll()
    suspend fun addPunicao(texto: String) = db.punicaoDao().insert(PunicaoEntity(texto = texto))

    // Raspadinhas
    // Raspadinhas
    suspend fun getRaspadinhas(): List<RaspadinhaEntity> = db.raspadinhaDao().getAll()
    suspend fun addRaspadinha(imagePath: String) = db.raspadinhaDao().insert(RaspadinhaEntity(imagePath = imagePath))
    suspend fun updateRaspadinha(r: RaspadinhaEntity) = db.raspadinhaDao().update(r)
    suspend fun deleteRaspadinha(r: RaspadinhaEntity) = db.raspadinhaDao().delete(r)

    companion object {
        @Volatile private var instance: DataRepository? = null
        fun getInstance(context: Context): DataRepository =
            instance ?: synchronized(this) {
                instance ?: DataRepository(context).also { instance = it }
            }
    }
}
