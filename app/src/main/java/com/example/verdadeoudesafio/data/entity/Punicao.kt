package com.example.verdadeoudesafio.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "punicoes")
data class PunicaoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val texto: String,
    val timestamp: Long = System.currentTimeMillis()
)
