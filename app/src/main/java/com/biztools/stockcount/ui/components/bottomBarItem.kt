package com.biztools.stockcount.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.biztools.stockcount.ui.theme.BackgroundDim
import com.biztools.stockcount.ui.utilities.NoRippleInteraction

@Composable
fun RowScope.BottomBarItem(
    isSelected: Boolean,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val colors = NavigationBarItemDefaults.colors(
        indicatorColor = BackgroundDim,
        unselectedTextColor = Color(0xFFFD9292),
        unselectedIconColor = Color(0xFFFD9292),
        selectedIconColor = Color.White,
        selectedTextColor = Color.White
    )
    this.NavigationBarItem(
        selected = isSelected,
        modifier = Modifier.height(70.dp),
        onClick = { onClick() },
        icon = { icon() },
//        label = { label() },
        colors = colors,
        interactionSource = NoRippleInteraction()
    )
}