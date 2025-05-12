package com.biztools.stockcount.models

data class SaveStockResult(
    val result: Boolean,
    val duplicateItems: List<DuplicateItem>,
    val notFoundItems: List<String>
)

data class DuplicateItem(
    val warehouse: String,
    val productNumber: String,
    val oldCount: Int,
    val newCount: Int
)
