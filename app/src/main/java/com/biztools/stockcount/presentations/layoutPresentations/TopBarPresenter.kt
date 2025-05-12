package com.biztools.stockcount.presentations.layoutPresentations

import android.content.Context
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import com.biztools.stockcount.stores.SettingStore
import com.biztools.stockcount.ui.layouts.TopBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TopBarPresenter(
    val ctx: Context,
    val scope: CoroutineScope,
    val setting: SettingStore,
    val navigator: NavHostController,
    val page: MutableState<String>,
    val drawer: DrawerState,
)
{
    fun openDrawer(callBackBefore: () -> Unit = {}, callBackAfter: () -> Unit = {}) {
        scope.launch {
            callBackBefore()
            drawer.open()
            callBackAfter()
        }
    }

    @Composable
    fun Render() {
        TopBar(this)
    }
}