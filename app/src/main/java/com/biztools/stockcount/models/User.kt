package com.biztools.stockcount.models

import java.util.Calendar

data class User(
    val username: String,
    val oid: String,
    val token: String,
    val password: String,
    val lastLogin: Calendar
)

data class UserResult(val username: String, val oid: String, val password: String)