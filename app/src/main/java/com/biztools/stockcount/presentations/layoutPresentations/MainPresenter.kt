package com.biztools.stockcount.presentations.layoutPresentations

import android.app.Activity
import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.biztools.stockcount.BuildConfig
import com.biztools.stockcount.R
import com.biztools.stockcount.api.AuthApi
import com.biztools.stockcount.api.Header
import com.biztools.stockcount.api.RestAPI
import com.biztools.stockcount.models.GetWarehousesResult
import com.biztools.stockcount.models.User
import com.biztools.stockcount.stores.SecurityStore
import com.biztools.stockcount.stores.SettingStore
import com.biztools.stockcount.stores.UserStore
import com.biztools.stockcount.ui.Main
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.system.exitProcess

class MainPresenter(
    val ctx: Context,
    val scope: CoroutineScope,
    val setting: SettingStore,
    val navigator: NavHostController,
    val page: MutableState<String>,
    val drawer: DrawerState
)
//    : BasePresenter(ctx, scope, setting, navigator, page, drawer)
{
    private var warehouses: MutableState<GetWarehousesResult?> = mutableStateOf(null)
    private var _failedValidation: MutableState<Boolean> = mutableStateOf(false)
    private var _initializing: MutableState<Boolean> = mutableStateOf(true)
    private var _initializeError: MutableState<Boolean> = mutableStateOf(false)
    private var _retryCount: MutableState<Int> = mutableIntStateOf(0)
    private var _needValidate: MutableState<Boolean> = mutableStateOf(false)
    private var _registering: MutableState<Boolean> = mutableStateOf(false)
    private var device: State<String?> = mutableStateOf(null)
    private var _user: State<User?> = mutableStateOf(null)
    private var _isDarkTheme: State<Boolean> = mutableStateOf(false)
    private var _secureStore: SecurityStore? = null
    private var _userStore: UserStore? = null
    private var isUnAuth = mutableStateOf(false)
    private var _device: State<String?> = mutableStateOf(null)
    private var _date: State<String> = mutableStateOf("")
    private var _msg: MutableState<String> = mutableStateOf("")
    private var _byRegister: MutableState<Boolean> = mutableStateOf(false)

    @Composable
    fun Content() {
        if (warehouses.value != null) DrawerPresenter(
            ctx,
            scope,
            setting,
            navigator,
            page,
            drawer,
            warehouses.value!!,
            device = device.value ?: "",
            _unAuthFromOutside = isUnAuth.value
        ).Render()
    }

    private var switch = mutableIntStateOf(0)
    private var isInteracted = mutableStateOf(true)

    @Composable
    fun Render() {
        _failedValidation = remember { mutableStateOf(false) }
        _isDarkTheme = remember { mutableStateOf(false) }
        _initializing = remember { mutableStateOf(true) }
        _initializeError = remember { mutableStateOf(false) }
        _registering = remember { mutableStateOf(false) }
        _secureStore = SecurityStore(ctx)
        _userStore = UserStore(ctx)
        _user = _userStore!!.user.collectAsState(initial = null)
        _byRegister = remember { mutableStateOf(false) }
        _retryCount = remember { mutableIntStateOf(0) }
        _date = remember { mutableStateOf("") }
        _msg = remember { mutableStateOf("") }
        switch = remember { mutableIntStateOf(0) }
        _device = _secureStore!!.device.collectAsState(initial = null)
        isInteracted = remember { mutableStateOf(true) }
        _needValidate = remember(_date.value, _device.value) {
            val noDevice = _device.value.isNullOrBlank() || _device.value.isNullOrEmpty()
            val noDate = _date.value.isEmpty() || _date.value.isBlank()
            val date = Calendar.getInstance()
            if (!noDate) date.timeInMillis = _date.value.toLongOrNull() ?: 0
            else date.timeInMillis = 0
            val valid = validDate(date, Calendar.getInstance()) && !noDate && !noDevice
            mutableStateOf(!valid)
        }
        warehouses = remember { mutableStateOf(null) }
        val onUnauth: () -> Unit = {
            _failedValidation.value = true
            _initializeError.value = false
            _retryCount.value = 0
            _initializing.value = false
        }
        val onSuccess: (r: GetWarehousesResult) -> Unit = {
            warehouses.value = it
            _retryCount.value = 0
            _initializeError.value = false
            _failedValidation.value = false
            _initializing.value = false
        }
//        LaunchedEffect(Unit) {
//            Toast.makeText(ctx, "api url: $apiUrl, license: $license", Toast.LENGTH_LONG).show()
//        }
        if (_initializing.value) {
            LaunchedEffect(Unit) {
                if (!_byRegister.value) {
                    delay(2000L)
                    initialize(onSuccess, onUnauth)
                }
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val transit = rememberInfiniteTransition(label = "loading")
                val density = LocalDensity.current
                val x = transit.animateFloat(
                    initialValue = with(density) { (-80).dp.toPx() },
                    targetValue = with(density) { 80.dp.toPx() },
                    animationSpec = infiniteRepeatable(
                        tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "loading"
                )
                Image(
                    painter = painterResource(id = R.drawable.best_code_scanner_logo),
                    contentDescription = "logo",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(25.dp))
                Box(
                    modifier = Modifier
                        .clipToBounds()
                        .width(80.dp)
                        .height(2.dp)
                        .background(Color(0x2A000000))
                ) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(2.dp)
                            .offset(Dp(x.value))
                            .background(Color(0xFF000000))
                    )
                }
                Spacer(modifier = Modifier.height(25.dp))
                Text("api url: ${BuildConfig.apiUrl}")
                Spacer(modifier = Modifier.height(5.dp))
                Text("license: ${BuildConfig.license}")
                Spacer(modifier = Modifier.height(5.dp))
                Text("device-id: " + (_device.value ?: "none"))
            }
        } else if (_initializeError.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        tint = Color.Red,
                        contentDescription = "failed",
                        modifier = Modifier.size(50.dp)
                    )
                    Text(text = _msg.value.ifEmpty { "Connection error!" })
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Button(
                            onClick = {
                                _initializeError.value = false
                                _initializing.value = true
                                _retryCount.value = 0
                                initialize(onSuccess, onUnauth)
                            },
                            modifier = Modifier.width(100.dp),
                            colors = ButtonDefaults.buttonColors()
                        ) { Text(text = "Retry") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                (ctx as Activity).finish()
                                exitProcess(0)
                            },
                            modifier = Modifier.width(100.dp),
                            colors = ButtonDefaults.buttonColors()
                        ) { Text(text = "Exit") }
                    }
                }
            }
        } else if (_failedValidation.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(10.dp)
                ) {
                    val token = remember { mutableStateOf("") }
                    val focusRequest = FocusRequester()
                    val focusManager = LocalFocusManager.current
                    Icon(
                        imageVector = Icons.Default.Warning,
                        tint = Color.Red,
                        contentDescription = "failed",
                        modifier = Modifier.size(50.dp)
                    )
                    Text(text = "App license verification is failed, you can't use this app!")
                    Text(
                        text = "If this app not yet registered, please register by input registered token in text box below",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = Color(0xFFA7A7A7),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LaunchedEffect(Unit) {
                            focusRequest.requestFocus()
                        }
                        BasicTextField(
                            value = token.value,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequest),
                            onValueChange = { token.value = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                registerApp(token.value, onSuccess)
                            }),
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Button(
                            onClick = { registerApp(token.value, onSuccess) },
                            colors = ButtonDefaults.buttonColors(
                                Color(0xFF597A32)
                            ),
                            enabled = token.value.isNotEmpty() && token.value.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) { Text(text = "Register") }
                        Button(
                            onClick = {
                                (ctx as Activity).finish()
                                exitProcess(0)
                            }, colors = ButtonDefaults.buttonColors(
                                Color(0xFFA53158)
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text(text = "Exit") }
                    }
                }
            }
        } else {
            Main(presenter = this)
        }
    }

    private fun registerApp(token: String, onSuccess: (r: GetWarehousesResult) -> Unit) {
        _failedValidation.value = false
        _initializeError.value = false
        _initializing.value = true
        _byRegister.value = true
        try {
            val registerApi =
                RestAPI.create<AuthApi>(headers = listOf(Header("conicalhat-license-token", token)))
            val registerCall = registerApi.createLicense(license = BuildConfig.license)
            RestAPI.execute(registerCall, scope, onSuccess = {
                _secureStore?.secure("")
                delay(500L)
                coreInit(onSuccess)
            }, onError = {
                _failedValidation.value = true
                _initializeError.value = false
                _initializing.value = false
            })
        } catch (_: Exception) {
            _failedValidation.value = true
            _initializeError.value = false
            _initializing.value = false
        }
    }

    private fun validDate(c1: Calendar, c2: Calendar): Boolean {
        c1.add(Calendar.DAY_OF_MONTH, 7)
        return c1.compareTo(c2) == 1
    }

    private fun coreInit(onSuccess: (r: GetWarehousesResult) -> Unit) {
        try {
            val authApi = RestAPI.create<AuthApi>(deviceId = _device.value ?: "")
            val vCall = authApi.validateAppAndGetWarehouses()
            RestAPI.execute(vCall, scope, onSuccess = { r ->
                _secureStore!!.secure(r.device)
                onSuccess(GetWarehousesResult(r.warehouses))
            }, onError = { e ->
                _msg.value = e.message ?: ""
                _failedValidation.value = true
                _initializeError.value = false
                _initializing.value = false
            })
        } catch (ex: Exception) {
            _msg.value = ex.message ?: ""
            _failedValidation.value = true
            _initializeError.value = false
            _initializing.value = false
        }
    }

    private fun initialize(
        onSuccess: (r: GetWarehousesResult) -> Unit,
        onUnauth: () -> Unit
    ) {
        _msg.value = ""
        if (_retryCount.value > 3) {
            _failedValidation.value = false
            _initializeError.value = true
            _initializing.value = false
            _retryCount.value = 0
            return
        }
        try {
            val authApi = RestAPI.create<AuthApi>(deviceId = _device.value ?: "")
            val vCall = authApi.validateAppAndGetWarehouses()
            RestAPI.execute(vCall, scope, onSuccess = { r ->
                _secureStore!!.secure(r.device)
                if (r.warehouses.isEmpty() && !r.warehouseError.isNullOrEmpty()) {
                    _failedValidation.value = false
                    _initializeError.value = true
                    _initializing.value = false
                    _msg.value = "couldn't get warehouses data, please check your system service"
                    _retryCount.value = 0
                } else onSuccess(GetWarehousesResult(r.warehouses))
            }, onError = { e ->
                _msg.value = e.message ?: e.cause?.message ?: ""
                if (e.message?.startsWith("unauth") == true) onUnauth()
                else {
                    _retryCount.value++
                    delay(2000)
                    initialize(onSuccess, onUnauth)
                }
            })
        } catch (ex: Exception) {
            _msg.value = ex.message ?: ex.cause?.message ?: ""
            _retryCount.value++
            scope.launch {
                delay(2000)
                initialize(onSuccess, onUnauth)
            }
        }
    }

    fun scrim(color: Color): Color = color.copy(alpha = 0f)
}