package com.biztools.stockcount.presentations.pagePresentations

import android.content.Context
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import com.biztools.stockcount.presentations.layoutPresentations.BasePresenter
import com.biztools.stockcount.stores.SettingStore
import com.biztools.stockcount.ui.pages.Menu
import kotlinx.coroutines.CoroutineScope

class MenuPresenter(
    ctx: Context? = null,
    scope: CoroutineScope? = null,
    setting: SettingStore? = null,
    navigator: NavHostController? = null,
    page: MutableState<String>? = null,
    drawer: DrawerState? = null
) : BasePresenter(ctx, scope, setting, navigator, page, drawer) {
    override val render: @Composable (content: (() -> Unit)?) -> Unit = {
        super.render { Menu(this) }
    }
    val content: @Composable () -> Unit = {}
    val toScan: () -> Unit = {
        navigator!!.navigate("scan")
    }
    val toCheckPrice: () -> Unit = {
        navigator!!.navigate("check-info")
    }

    val toPrintLabel: () -> Unit = {
        navigator!!.navigate("label")
    }
    val toPo: () -> Unit = {
        navigator!!.navigate("po")
    }
}