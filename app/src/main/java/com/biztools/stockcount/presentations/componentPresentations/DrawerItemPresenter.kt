package com.biztools.stockcount.presentations.componentPresentations

import androidx.compose.runtime.Composable
import com.biztools.stockcount.ui.components.DrawerItem

class DrawerItemPresenter(
    private val _selected: Boolean = false,
    private val _label: (@Composable () -> Unit)? = null,
    private val _icon: (@Composable () -> Unit)? = null,
    private val _onClick: (() -> Unit)? = null
) {
    val label get() = _label
    val icon get() = _icon
    val onClick get() = _onClick
    val isSelected get() = _selected
    val render: @Composable (content: (() -> Unit)?) -> Unit
        get() = { DrawerItem(this) }
}