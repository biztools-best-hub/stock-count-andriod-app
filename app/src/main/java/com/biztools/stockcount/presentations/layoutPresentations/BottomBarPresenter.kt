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
    val ctx: Context? = null,
    val scope: CoroutineScope? = null,
    val setting: SettingStore? = null,
    val navigator: NavHostController? = null,
    val page: MutableState<String>? = null,
    val drawer: DrawerState? = null,
)
{
    @Composable
    fun Render() {
        BottomBar(this)
    }

    @Composable
    fun RenderItem(
        scope: RowScope,
        selected: Boolean,
        icon: @Composable () -> Unit,
        onClick: () -> Unit
    ) {
        scope.BottomBarItem(
            isSelected = selected,
            icon = icon,
        ) { onClick() }
    }

    fun navigateTo(route: String) {
        if (navigator!!.currentDestination?.route != route) {
            if (route == "menu") navigator.navigate("menu") {
                popUpTo("menu") {
                    inclusive = true
                }
            }
            else navigator.navigate(route)
        }
    }
}