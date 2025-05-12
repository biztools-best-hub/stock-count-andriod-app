package com.biztools.stockcount.models

data class StockOrderInput(val orders: MutableList<StockOrder> = mutableListOf())
data class StockOrder(
    val itemOid: String,
    val itemNumber: String,
    val qty: Int,
    val warehouse: String,
    val s1: String = "",
    val s2: String = "",
    val s4: String = "",
    val s5: String = "",
    val s6: String = "",
    val s7: String = "",
    val s8: String = "",
    val s9: String = "",
    val s10: String = "",
)
