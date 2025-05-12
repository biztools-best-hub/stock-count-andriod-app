package com.biztools.stockcount.models

data class PrintBarcodesInput(
    var rateCardOid: String?,
    val barCodes: MutableList<String> = mutableListOf()
)
