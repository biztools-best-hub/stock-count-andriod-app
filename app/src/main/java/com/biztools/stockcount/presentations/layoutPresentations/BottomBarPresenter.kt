package com.biztools.stockcount.presentations.layoutPresentations

import android.content.Context
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import com.biztools.stockcount.stores.SettingStore
import com.biztools.stockcount.ui.components.BottomBarItem
import com.biztools.stockcount.ui.layouts.BottomBar
import kotlinx.coroutines.CoroutineScope

class BottomBarPresenter(
    ctx: Context? = null,
    scope: CoroutineScope? = null,
    setting: SettingStore? = null,
    navigator: NavHostController? = null,
    page: MutableState<String>? = null,
    drawer: DrawerState? = null,
) : BasePresenter(ctx, scope, setting, navigator, page, drawer) {
    override val render: @Composable (content: (() -> Unit)?) -> Unit = {
        super.render { BottomBar(this) }
    }
    val renderItem: @Composable (
        scope: RowScope,
        selected: Boolean,
        label: @Composable () -> Unit,
        icon: @Composable () -> Unit,
        onClick: () -> Unit
    ) -> Unit = { scope, selected, label, icon, click ->
        scope.BottomBarItem(
            isSelected = selected,
            icon = icon,
            label = label
        ) { click() }
    }

    fun navigateTo(route: String) {
        if (navigator!!.currentDestination?.route != route) {
            if (route == "menu") navigator!!.navigate("menu") {
                popUpTo("menu") {
                    inclusive = true
                }
            }
            else navigator!!.navigate(route)
        }
    }
}