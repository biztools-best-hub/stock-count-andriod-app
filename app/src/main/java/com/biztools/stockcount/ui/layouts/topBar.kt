@file:OptIn(ExperimentalMaterial3Api::class)

package com.biztools.stockcount.ui.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biztools.stockcount.presentations.layoutPresentations.TopBarPresenter
import com.biztools.stockcount.ui.extensions.customShadow
import com.biztools.stockcount.ui.theme.BackgroundAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(presenter: TopBarPresenter) {
    val title = remember(presenter.page!!.value) {
        var manipulate = when (presenter.page!!.value) {
            "menu" -> "Home"
            "codes" -> "Unsaved Count"
            "scan" -> "Count Stock"
            "label" -> "Print Label"
            "po" -> "Purchase Order"
            "config" -> "PO Config"
            else -> presenter.page!!.value.replaceFirstChar { it.uppercase() }
        }
        if (manipulate.lowercase().startsWith("add-item")) manipulate = "Add Item"
        mutableStateOf(manipulate)
    }
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text(text = title.value, fontSize = 16.sp)
            }
        },
        windowInsets = WindowInsets(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        ),
        navigationIcon = {
            IconButton(onClick = { presenter.openDrawer() }) {
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "drawer-menu")
            }
        },
        modifier = Modifier
            .customShadow(x = 0f, y = 1f, alpha = .2f)
            .height(
                WindowInsets.statusBars
                    .asPaddingValues()
                    .calculateTopPadding() + 40.dp
            ),
        actions = {},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BackgroundAccent,
            navigationIconContentColor = Color.White,
            titleContentColor = Color.White
        )
    )
}