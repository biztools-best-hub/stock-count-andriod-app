package com.biztools.stockcount.presentations.pagePresentations

import android.content.Context
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import com.biztools.stockcount.stores.SettingStore
import com.biztools.stockcount.ui.pages.Menu
import kotlinx.coroutines.CoroutineScope

class MenuPresenter(
    val ctx: Context? = null,
    val scope: CoroutineScope? = null,
    val setting: SettingStore? = null,
    val navigator: NavHostController? = null,
    val page: MutableState<String>? = null,
    val drawer: DrawerState? = null
)
{
    @Composable
    fun Render() {
        Menu(this)
    }

    val toScan: () -> Unit = {
        navigator!!.navigate("scan")
    }
    val toCheckPromo: () -> Unit = {
        navigator!!.navigate("check-promo/..")
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
    val toOnHand: () -> Unit = {
        navigator!!.navigate("on-hand/..")
    }
}