package com.biztools.stockcount.presentations.layoutPresentations

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.biztools.stockcount.stores.SettingStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class BasePresenter(
    private var _ctx: Context? = null,
    private var _scope: CoroutineScope? = null,
    private var _setting: SettingStore? = null,
    private var _navigator: NavHostController? = null,
    private var _page: MutableState<String>? = null,
    private var _drawer: DrawerState? = null
) {
    val ctx: Context? get() = _ctx
    val scope: CoroutineScope? get() = _scope
    val setting: SettingStore? get() = _setting
    val navigator: NavHostController? get() = _navigator
    val page: MutableState<String>? get() = _page
    val drawer: DrawerState? get() = _drawer
    val isDrawerOpen get() = _drawer?.isOpen ?: false
    private var _isDarkTheme: State<Boolean> = mutableStateOf(false)
    val isDarkTheme get() = _isDarkTheme.value
    open val render: @Composable (content: (@Composable () -> Unit)?) -> Unit
        get() = {
            if (_ctx == null) _ctx = LocalContext.current
            if (_scope == null) _scope = rememberCoroutineScope()
            if (_setting == null) _setting = SettingStore(_ctx!!)
            if (_navigator == null) _navigator = rememberNavController()
            if (_drawer == null) _drawer = rememberDrawerState(initialValue = DrawerValue.Closed)
            BackHandler(page!!.value != "menu") {
                navigator!!.navigate("menu") {
                    if (navigator!!.currentDestination != null) popUpTo(
                        navigator!!.currentDestination!!.id
                    ) { inclusive = true }
                }
            }
            if (it != null) it()
        }

    fun closeDrawer(callBackBefore: () -> Unit = {}, callBackAfter: () -> Unit = {}) {
        _scope!!.launch {
            callBackBefore()
            _drawer!!.close()
            callBackAfter()
        }
    }

    fun openDrawer(callBackBefore: () -> Unit = {}, callBackAfter: () -> Unit = {}) {
        _scope!!.launch {
            callBackBefore()
            _drawer!!.open()
            callBackAfter()
        }
    }
}