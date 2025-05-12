package com.biztools.stockcount.models

import java.util.Date

data class StockCountBatchInput(
    val items: MutableList<StockCountInput>,
    val duplicateItems: MutableList<DuplicateItemInput>
)

data class StockCountInput(
    val warehouse: String,
    val itemNumber: String,
    val count: Int,
    val timestamp: Date
)

data class DuplicateItemInput(
    val warehouse: String,
    val productNumber: String,
    var decision: String = "m"
)