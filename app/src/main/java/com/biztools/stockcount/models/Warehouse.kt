package com.biztools.stockcount.models

data class Warehouse(val oid: String, val name: String)
data class GetWarehousesResult(val warehouses: List<Warehouse> = mutableListOf())
