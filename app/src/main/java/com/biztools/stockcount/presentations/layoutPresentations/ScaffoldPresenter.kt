package com.biztools.stockcount.presentations.layoutPresentations

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.biztools.stockcount.models.GetWarehousesResult
import com.biztools.stockcount.stores.SettingStore
import com.biztools.stockcount.ui.components.QRDialog
import com.biztools.stockcount.ui.layouts.MainScaffold
import com.biztools.stockcount.ui.layouts.NavigationScaffold
import kotlinx.coroutines.CoroutineScope

class ScaffoldPresenter(
    ctx: Context? = null,
    scope: CoroutineScope? = null,
    setting: SettingStore? = null,
    navigator: NavHostController? = null,
    page: MutableState<String>? = null,
    drawer: DrawerState? = null,
    private val warehouses: GetWarehousesResult,
    isUnauth: Boolean = true,
    onUnauth: () -> Unit,
    onAuth: () -> Unit
) : BasePresenter(ctx, scope, setting, navigator, page, drawer) {
    private var _isQRShow: MutableState<Boolean> = mutableStateOf(false)
    private val closeQR: () -> Unit = { _isQRShow.value = false }
    val content: @Composable () -> Unit = {
        _isQRShow = remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            NavigationScaffold(
                ctx = ctx!!,
                scope = scope!!,
                setting = setting!!,
                navigator = navigator!!,
                page = page!!,
                drawer = drawer!!,
                warehouses,
                isUnauth = isUnauth,
                onUnauth = onUnauth,
                onAuth = onAuth
            )
        }
    }
    override val render: @Composable (content: (() -> Unit)?) -> Unit = {
        super.render {
            MainScaffold(
                this,
                TopBarPresenter(ctx, scope, setting, navigator, page, drawer),
                BottomBarPresenter(ctx, scope, setting, navigator, page, drawer)
            )
        }
    }
    val renderQR: @Composable () -> Unit = { if (_isQRShow.value) QRDialog { closeQR() } }
}