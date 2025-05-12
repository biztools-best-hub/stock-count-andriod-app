package com.biztools.stockcount.models

data class ValidationAndWarehouses(val device: String, val warehouses: List<Warehouse>)
data class ValidationAndWhoAmI(val device: String, val user: UserResult)