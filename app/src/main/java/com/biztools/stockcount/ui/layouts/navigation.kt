package com.biztools.stockcount.ui.layouts

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.biztools.stockcount.models.GetWarehousesResult
import com.biztools.stockcount.presentations.layoutPresentations.AuthPresenter
import com.biztools.stockcount.presentations.pagePresentations.MenuPresenter
import com.biztools.stockcount.presentations.pagePresentations.PriceCheckPresenter
import com.biztools.stockcount.presentations.pagePresentations.ReservedCodesPresenter
import com.biztools.stockcount.presentations.pagePresentations.ScanPresenter
import com.biztools.stockcount.stores.SettingStore
import com.biztools.stockcount.stores.UserStore
import com.biztools.stockcount.ui.pages.AddItem
import com.biztools.stockcount.ui.pages.CheckPromo
import com.biztools.stockcount.ui.pages.POConfig
import com.biztools.stockcount.ui.pages.Po
import com.biztools.stockcount.ui.pages.PrintLabel
import com.biztools.stockcount.ui.pages.OnHand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun NavigationScaffold(
    ctx: Context,
    scope: CoroutineScope,
    setting: SettingStore,
    navigator: NavHostController,
    page: MutableState<String>,
    drawer: DrawerState,
    warehouses: GetWarehousesResult,
    onUnauth: () -> Unit,
    onAuth: () -> Unit
) {
    val isDark = setting.isDark.collectAsState(initial = false).value
    val userStore = UserStore(ctx)
    val user = userStore.user.collectAsState(initial = null).value
    BackHandler(drawer.isOpen) { scope.launch { drawer.close() } }
    NavHost(navController = navigator, startDestination = "menu") {
        composable("check-info") {
            PriceCheckPresenter(
                ctx,
                scope,
                setting,
                navigator,
                page,
                drawer,
                warehouses,
                onUnauth = onUnauth,
                onAuth = onAuth
            ).Render()
        }
        composable("check-promo/{number}") {
            CheckPromo(ctx, navigator, drawer, scope)
        }
        composable("on-hand/{number}") {
            OnHand(ctx, navigator, drawer, scope)
        }
        composable("menu") {
            MenuPresenter(
                ctx,
                scope,
                setting,
                navigator,
                page,
                drawer
            ).Render()
        }
        composable("codes") {
            ReservedCodesPresenter(
                ctx,
                scope,
                setting,
                navigator,
                page,
                drawer,
            ).Render()
        }
        composable("scan") {
            ScanPresenter(
                ctx,
                scope,
                setting,
                navigator,
                page,
                drawer,
                warehouses
            ).Render()
        }
        composable("label") {
            AuthPresenter(
                ctx,
                scope,
                darkTheme = isDark,
                navigator,
                drawer,
                onAuth = onAuth
            ) {
                PrintLabel(ctx, scope, isDark, user, onUnauth = onUnauth)
            }.Initialize()
        }
        composable("po") {
            AuthPresenter(
                ctx,
                scope,
                darkTheme = isDark,
                navigator,
                drawer,
                onAuth = onAuth
            ) {
                Po(ctx, scope, isDark, user, page.value, navigator, onUnauth = onUnauth)
            }.Initialize()
        }
        composable("config") { POConfig(ctx, scope) }
        composable("add-item?code={code}&fromRoute={fromRoute}", listOf(
            navArgument("code") { defaultValue = "" },
            navArgument("fromRoute") { defaultValue = "" }
        )) {
            AuthPresenter(
                ctx,
                scope,
                darkTheme = isDark,
                navigator,
                drawer,
                onAuth = onAuth
            ) {
                AddItem(
                    ctx,
                    scope,
                    user,
                    navigator,
                    it.arguments?.getString("code"),
                    it.arguments?.getString("fromRoute"),
                    onUnauth = onUnauth
                )
            }.Initialize()
        }
    }
}