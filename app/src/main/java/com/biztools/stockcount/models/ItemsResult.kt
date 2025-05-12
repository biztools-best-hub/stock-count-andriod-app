package com.biztools.stockcount.models

data class ItemsResult(val items: String = "", val isSuccess: Boolean = false)
data class Item(
    val itemNumber: String,
    val barcode: String,
    val name: String,
    val useKit: Boolean,
    val description: String
)
