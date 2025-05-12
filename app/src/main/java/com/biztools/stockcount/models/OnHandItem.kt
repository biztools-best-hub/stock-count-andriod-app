package com.biztools.stockcount.models

data class OnHandItem(
    val name: String,
    val number: String,
    val useKit: Boolean,
    val description: String,
    val code: String,
)
