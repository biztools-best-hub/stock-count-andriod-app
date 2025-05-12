package com.biztools.stockcount.models

data class ItemInfo(
    val oid: String,
    val number: String,
    val name: String,
    val qty: Int,
    val total: Int,
    val reorderPoint: Float,
    val poQty: Int,
    val minimumQtyOrder: Int,
    val diffQty: Int,
    val reorderQty: Int,
    val saleRate: Float,
    val price: Float,
    val avgCost: Float,
    val currency: String,
    val supplier: String
)
