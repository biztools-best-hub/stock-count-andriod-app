package com.biztools.stockcount.models

data class PromotionItem(
    val name: String,
    val promotionBoughtQty: Int,
    val promotionFreeQty: Int,
    val promotionLimitedQty: Double,
)
