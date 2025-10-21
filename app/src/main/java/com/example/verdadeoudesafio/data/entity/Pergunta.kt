package com.example.verdadeoudesafio.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.verdadeoudesafio.admin.TextLevelItem

@Entity(tableName = "perguntas")
data class PerguntaEntity(

    @PrimaryKey(autoGenerate = true) override val id: Int = 0,
    override val texto: String,
    override val level: Int,
    val timestamp: Long = System.currentTimeMillis()
): TextLevelItem

