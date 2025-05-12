package com.biztools.stockcount.models

data class ItemOnHandInWarehouse(
    val location: String,
    val description: String,
    val qty: Double,
    val onHold: Double,
    val available: Double,
)