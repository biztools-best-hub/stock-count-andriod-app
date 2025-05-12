package com.biztools.stockcount.ui.components

import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.runtime.Composable

@Composable
fun DrawerItem(
    label: (@Composable () -> Unit)?,
    icon: (@Composable () -> Unit)?,
    isSelected: Boolean,
    onClick: (() -> Unit)?
) {
    NavigationDrawerItem(
        label = { label?.let { it() } },
        icon = { icon?.let { it() } },
        selected = isSelected,
        onClick = { onClick?.let { it() } })
}