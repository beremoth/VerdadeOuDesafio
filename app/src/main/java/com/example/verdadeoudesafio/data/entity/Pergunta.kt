package com.example.verdadeoudesafio.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perguntas")
data class PerguntaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val texto: String,
    val level: Int,
    val timestamp: Long = System.currentTimeMillis()
)
