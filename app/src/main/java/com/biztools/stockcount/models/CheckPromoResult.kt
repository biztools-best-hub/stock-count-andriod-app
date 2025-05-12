package com.biztools.stockcount.models

data class CheckPromoResult(
    var number: String,
    val name: String,
    val group: String?,
    val pdtCat: String?,
    val subCat1: String?,
    val subCat2: String?,
    val subCat3: String?,
    val brand: String?,
    val description: String?,
    val description2: String?,
    val retailPrice: Double,
    val wholeSalePrice: Double,
    val unitCost: Double,
    val currencyName: String?,
    val currencySymbol: String?,
    val totalSOH: Double,
    val promotions: List<Promotion> = listOf()
)