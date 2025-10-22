package com.example.verdadeoudesafio.admin

sealed class AdminListItem {
    data class Header(val level: Int) : AdminListItem()
    data class Item(val item: TextLevelItem) : AdminListItem()
}