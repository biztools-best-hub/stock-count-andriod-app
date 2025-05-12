package com.biztools.stockcount.presentations.layoutPresentations

import android.content.Context
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import com.biztools.stockcount.stores.SettingStore
import com.biztools.stockcount.ui.layouts.TopBar
import kotlinx.coroutines.CoroutineScope

class TopBarPresenter(
    ctx: Context? = null,
    scope: CoroutineScope? = null,
    setting: SettingStore? = null,
    navigator: NavHostController? = null,
    page: MutableState<String>? = null,
    drawer: DrawerState? = null,
) : BasePresenter(ctx, scope, setting, navigator, page, drawer) {
    override val render: @Composable (content: (() -> Unit)?) -> Unit = {
        super.render { TopBar(this) }
    }
}