package com.example.verdadeoudesafio.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "raspadinhas")
data class RaspadinhaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // Caminho da imagem local (ex: /storage/emulated/0/Android/data/... ou content://)
    val imagePath: String,

    // Pode ser usado para ordenar ou exibir data de criação
    val timestamp: Long = System.currentTimeMillis()

)
