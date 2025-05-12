package com.biztools.stockcount.models

import androidx.compose.material3.DrawerState
import androidx.navigation.NavHostController

data class UiProps(
    val page: String = "menu",
    val isDark: Boolean = false,
    val isAutoScan: Boolean = false,
    val drawer: DrawerState,
    val warehouses: GetWarehousesResult,
    val navigator: NavHostController,
    val user: User?
)