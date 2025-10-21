package com.example.verdadeoudesafio.admin

// Interface para itens de texto que possuem nível e, opcionalmente, tempo
interface TextLevelItem {
    val id: Int // Ou Long, dependendo da sua chave primária
    val texto: String
    val level: Int
    val tempo: Int? // Tempo opcional (será null para Pergunta e Punição)
        get() = null // Implementação padrão retorna null
}