package com.example.verdadeoudesafio.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.verdadeoudesafio.admin.TextLevelItem

@Entity(tableName = "desafios")
data class DesafioEntity(
    @PrimaryKey(autoGenerate = true) override val id: Int = 0,
    override val texto: String,
    override val level: Int,
    override val tempo: Int // Campo original
) : TextLevelItem
