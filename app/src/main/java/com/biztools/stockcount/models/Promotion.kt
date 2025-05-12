package com.biztools.stockcount.models

data class Promotion(
    val name: String,
    val period: String,
    val qty: Double,
    val discount: Double,
    val hasCashback: Boolean,
    val isDiscountPercentage: Boolean,
    val cashback: Double,
    val promotionItems: List<PromotionItem> = listOf(),
    val promotionPrice: Double,
    val limitedCashbackSaleQty: Double,
    val isCashbackPercentage: Boolean
)
