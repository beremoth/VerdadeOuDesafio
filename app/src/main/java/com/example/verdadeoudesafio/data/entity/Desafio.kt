package com.example.verdadeoudesafio.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "desafios")
data class DesafioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val texto: String,
    val level: Int,
    val tempo: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
