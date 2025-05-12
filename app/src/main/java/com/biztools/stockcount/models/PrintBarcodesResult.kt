package com.biztools.stockcount.models

data class PrintBarcodesResult(val isSuccess: Boolean, val notFoundCodes: List<String> = listOf())
