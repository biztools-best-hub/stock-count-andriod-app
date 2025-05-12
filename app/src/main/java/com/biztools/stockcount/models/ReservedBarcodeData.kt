package com.biztools.stockcount.models

import java.util.Calendar


data class ReservedBarcodeData(
    val code: String,
    var count: Int = 1,
    val warehouse: String,
    val timestamp: Calendar = Calendar.getInstance()
)
