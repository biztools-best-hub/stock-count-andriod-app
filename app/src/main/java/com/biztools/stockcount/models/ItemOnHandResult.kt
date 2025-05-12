package com.biztools.stockcount.models

data class ItemOnHandResult(
    var number: String,
    val name: String,
    val oid: String,
    val description: String,
    val description2: String,
    val price: Double,
    val brand: String,
    val group: String,
    val wholeSalePrice: Double,
    val unitCost: Double,
    val pdtCat: String,
    val subCat1: String,
    val subCat2: String,
    val subCat3: String,
    val totalSOH: Double,
    val items: List<OnHandItem> = listOf(),
    val warehouses: List<ItemOnHandInWarehouse> = listOf()
)