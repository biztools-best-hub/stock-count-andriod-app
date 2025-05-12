package com.biztools.stockcount.presentations.layoutPresentations

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.biztools.stockcount.R
import com.biztools.stockcount.api.AuthApi
import com.biztools.stockcount.api.RestAPI
import com.biztools.stockcount.api.StockApi
import com.biztools.stockcount.models.GetWarehousesResult
import com.biztools.stockcount.models.LoginInput
import com.biztools.stockcount.models.User
import com.biztools.stockcount.stores.SettingStore
import com.biztools.stockcount.stores.UserStore
import com.biztools.stockcount.ui.components.CircularLoading
import com.biztools.stockcount.ui.components.DrawerItem
import com.biztools.stockcount.ui.layouts.Drawer
import com.biztools.stockcount.ui.utilities.NoRippleInteraction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

class DrawerPresenter(
    val ctx: Context,
    val scope: CoroutineScope,
    val setting: SettingStore,
    val navigator: NavHostController,
    val page: MutableState<String>,
    val drawer: DrawerState,
    private val warehouses: GetWarehousesResult,
    private val device: String = "",
    private val _unAuthFromOutside: Boolean = false
)
//    : BasePresenter(ctx, scope, setting, navigator, page, drawer)
{
    private var _isAutoScan: State<Boolean> = mutableStateOf(false)
    private var _showLogin = mutableStateOf(false)
    val showLogin get() = _showLogin.value
    val isAutoScan get() = _isAutoScan
    private var _isUnauth: MutableState<Boolean> = mutableStateOf(true)
    private var _user: State<User?> = mutableStateOf(null)
    private var userStore: UserStore? = null
    private var _loggingIn = mutableStateOf(false)
    private var _unError = mutableStateOf(false)
    private var _pwError = mutableStateOf(false)
    private var _downloading = mutableStateOf(false)
    private var _downloadSuccess = mutableStateOf(false)
    private var _pendingDownload = mutableStateOf(false)

    private var _rawItems = mutableStateListOf<String>()
    private var _failedDownload = mutableStateOf(false)
    private var _showFile = mutableStateOf(false)

    val isUnauth get() = _isUnauth.value
    val user get() = _user.value
    val downloading get() = _downloading.value
    val failedDownload get() = _failedDownload.value
    val downloadSuccess get() = _downloadSuccess.value
    private val onUnauth: () -> Unit = {
        _isUnauth.value = true
        onLogout()
    }
    private val onAuth: () -> Unit = {
        _isUnauth.value = false
    }

    @Composable
    fun Content() {
        Box(modifier = Modifier.fillMaxSize()) {
            ScaffoldPresenter(
                ctx,
                scope,
                setting,
                navigator,
                page,
                drawer,
                device = device,
                warehouses,
                onUnauth = onUnauth,
                onAuth = onAuth
            ).Render()
        }
    }

    fun closeDrawer(callBackBefore: () -> Unit = {}, callBackAfter: () -> Unit = {}) {
        scope.launch {
            callBackBefore()
            drawer.close()
            callBackAfter()
        }
    }

    @Composable
    fun LoginBox(dev: String) {
        val un = remember { mutableStateOf("") }
        val pw = remember { mutableStateOf("") }
        val showPassword = remember { mutableStateOf(false) }
        Dialog(
            onDismissRequest = { _showLogin.value = false }, DialogProperties(
                dismissOnClickOutside = !_loggingIn.value,
                dismissOnBackPress = !_loggingIn.value,
            )
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .fillMaxWidth()
                    .background(Color(0xFFFFFFFF))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                val focus = LocalFocusManager.current
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Username")
                        Row(
                            Modifier
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF8D8C8C),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "username"
                            )
                            BasicTextField(
                                modifier = Modifier.weight(1f),
                                value = un.value,
                                onValueChange = {
                                    un.value = it
                                    if (_pwError.value) _pwError.value = false
                                    if (_unError.value) _unError.value = false
                                },
                                enabled = !_loggingIn.value,
                                singleLine = true,
                                keyboardActions = KeyboardActions(onDone = {
                                    focus.moveFocus(FocusDirection.Down)
                                })
                            )
                        }
                        if (_unError.value) Text(
                            text = "Invalid username",
                            color = Color(0xFFE41616),
                            fontSize = 10.sp
                        )

                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Password")
                        Row(
                            Modifier
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF8D8C8C),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "password"
                            )
                            BasicTextField(
                                modifier = Modifier.weight(1f),
                                value = pw.value, onValueChange = {
                                    pw.value = it
                                    if (_pwError.value) _pwError.value = false
                                    if (_unError.value) _unError.value = false
                                },
                                enabled = !_loggingIn.value,
                                singleLine = true,
                                keyboardActions = KeyboardActions(onDone = {
                                    onLogin(un.value, pw.value, dev)
                                }),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (!showPassword.value) KeyboardType.Password
                                    else KeyboardType.Text
                                ),
                                visualTransformation = if (!showPassword.value) PasswordVisualTransformation()
                                else VisualTransformation.None
                            )
                            Icon(
                                painter = painterResource(
                                    id = if (!showPassword.value) R.drawable.visible
                                    else R.drawable.invisible
                                ),
                                contentDescription = "visibility",
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable(
                                        interactionSource = NoRippleInteraction(),
                                        null,
                                        onClick = {
                                            if (!_loggingIn.value)
                                                showPassword.value = !showPassword.value
                                        })
                            )
                        }
                        if (_pwError.value) Text(
                            text = "Incorrect password",
                            color = Color(0xFFE41616),
                            fontSize = 10.sp
                        )
                    }
                    Column {
                        Button(
                            onClick = {
                                focus.clearFocus()
                                onLogin(un.value, pw.value, dev)
                            },
                            enabled = !_loggingIn.value,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (_loggingIn.value)
                                    CircularLoading(modifier = Modifier.size(20.dp))
                                Text(text = "Login")
                            }
                        }
                        Button(
                            onClick = { _showLogin.value = false },
                            enabled = !_loggingIn.value,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(5.dp)
                        ) { Text(text = "Cancel") }
                    }
                }
            }
        }
    }

    val nowLogin: () -> Unit = { _showLogin.value = true }

    @Composable
    fun Render() {
        _isAutoScan = setting.isAutoScanMode.collectAsState(initial = false)
        _showLogin = remember { mutableStateOf(false) }
        _downloading = remember { mutableStateOf(false) }
        _downloadSuccess = remember { mutableStateOf(false) }
        _rawItems = remember { mutableStateListOf() }
        _pendingDownload = remember { mutableStateOf(false) }
        _showFile = remember { mutableStateOf(false) }
        _isUnauth = remember { mutableStateOf(true) }
        _loggingIn = remember { mutableStateOf(false) }
        _unError = remember { mutableStateOf(false) }
        _pwError = remember { mutableStateOf(false) }
        userStore = UserStore(ctx)
        _user = userStore!!.user.collectAsState(initial = null)
        LaunchedEffect(_unAuthFromOutside) {
            if (_unAuthFromOutside) onUnauth()
        }
        LaunchedEffect(_user.value) {
            _isUnauth.value = _user.value == null
        }
        Drawer(this)
    }

    val toggleScanMode: () -> Unit = {
        scope.launch { setting.toggleScanMode() }
    }

    @Composable
    fun RenderItem(
        selected: Boolean,
        label: (@Composable () -> Unit)?,
        icon: (@Composable () -> Unit)?,
        onClick: (() -> Unit)?
    ) {
        DrawerItem(label = label, icon = icon, isSelected = selected, onClick)
    }

    private fun onLogin(username: String, password: String, dev: String) {
        _loggingIn.value = true
        try {
            val api = RestAPI.create<AuthApi>(deviceId = dev)
            val call = api.login(LoginInput(username, password))
            RestAPI.execute(call, scope, onSuccess = { r ->
                _loggingIn.value = false
                userStore!!.setUser(r.user.username, r.user.oid, r.token, r.user.password)
                _showLogin.value = false
                _isUnauth.value = false
                if (_pendingDownload.value) {
                    onDownload(dev)
                    _pendingDownload.value = false
                }
            }, onError = { e ->
                _unError.value = true
                _pwError.value = true
                _loggingIn.value = false
                Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
            })
        } catch (e: Exception) {
            _unError.value = true
            _pwError.value = true
            _loggingIn.value = false
            Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
        }
    }

    val onLogout: () -> Unit = {
        scope.launch {
            if (drawer.isOpen) drawer.close()
            userStore?.removeUser()
        }
    }

    fun closeLoading() {
        _downloading.value = false
        _failedDownload.value = false
        _downloadSuccess.value = false
    }

    private fun download(page: Int, dev: String) {
        try {
            val api = RestAPI.create<StockApi>(user?.token, deviceId = dev)
            val call = api.getItems(page, count = 500)
            RestAPI.execute(call, scope, onSuccess = { r ->
                try {
                    if (r.items.isEmpty()) {
                        _failedDownload.value = false
                        _downloadSuccess.value = true
                        _downloading.value = false
                        val fileName = "conicalhat-items"
                        val fileContents = _rawItems.joinToString("\n")
                        val file = File(ctx.filesDir, fileName)
                        if (file.exists()) file.delete()
                        file.writeText(fileContents)
                    } else {
                        _rawItems.addAll(r.items.split("\n").sorted())
                        download(page + 1, dev)
                    }
                } catch (ex: Exception) {
                    _failedDownload.value = true
                    _downloading.value = false
                }
            }) { e ->
                Toast.makeText(ctx, e.message, Toast.LENGTH_SHORT).show()
                if (e.message?.startsWith("unauth") == true) onUnauth()
                else _failedDownload.value = true
                _downloading.value = false
            }
        } catch (e: Exception) {
            _failedDownload.value = true
            _downloading.value = false
        }
    }

    fun onDownload(dev: String) {
        if (user == null) {
            _showLogin.value = true
            _pendingDownload.value = true
            return
        }
        _downloading.value = true
        _rawItems.clear()
        download(1, dev)
    }
}