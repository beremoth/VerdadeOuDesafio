package com.example.verdadeoudesafio.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "raspadinhas")
data class Raspadinha(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imagemPath: String // caminho da imagem salva
)
