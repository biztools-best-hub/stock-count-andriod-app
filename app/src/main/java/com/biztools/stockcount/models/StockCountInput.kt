package com.biztools.stockcount.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

data class ExtendDuplicateItemInput(
    val productNumber: String,
    var decision: String = "m",
    var offset: Dp = 0.dp,
    var color: Color = Color(0xFF3A3A3A)
)
