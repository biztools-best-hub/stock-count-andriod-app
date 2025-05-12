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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.biztools.stockcount.R
import com.biztools.stockcount.api.AuthApi
import com.biztools.stockcount.api.RestAPI
import com.biztools.stockcount.models.LoginInput
import com.biztools.stockcount.models.User
import com.biztools.stockcount.stores.SecurityStore
import com.biztools.stockcount.stores.UserStore
import com.biztools.stockcount.ui.components.CircularLoading
import com.biztools.stockcount.ui.utilities.NoRippleInteraction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar

class AuthPresenter(
    private val ctx: Context,
    private val scope: CoroutineScope,
    private val darkTheme: Boolean,
    private val navigator: NavHostController,
    private val drawer: DrawerState,
    private val onAuth: () -> Unit = {},
    private val authorizedContent: @Composable () -> Unit
) {
    private var _un = mutableStateOf("")
    private var _pw = mutableStateOf("")
    private var _unError = mutableStateOf(false)
    private var _pwError = mutableStateOf(false)
    private var _loggingIn = mutableStateOf(false)
    private var _store: UserStore? = null
    private var _closeNow = mutableStateOf(false)

    @Composable
    fun Initialize() {
        _store = UserStore(ctx)
        val user = _store!!.user
            .collectAsState(initial = User("", "", "", "", Calendar.getInstance()))
        val focus = LocalFocusManager.current
        val showPassword = remember { mutableStateOf(false) }
        val dev = SecurityStore(ctx).device.collectAsState(initial = null)
        _loggingIn = remember { mutableStateOf(false) }
        _closeNow = remember { mutableStateOf(false) }
        _un = remember { mutableStateOf("") }
        _pw = remember { mutableStateOf("") }
        _unError = remember { mutableStateOf(false) }
        _pwError = remember { mutableStateOf(false) }
        LaunchedEffect(true) {
            if (!drawer.isClosed) drawer.close()
        }
        authorizedContent()
        if (user.value == null && !_closeNow.value) {
            Dialog(
                onDismissRequest = { onCancel() },
                DialogProperties()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .fillMaxWidth()
                        .background(
                            if (darkTheme) Color(0xFF222222)
                            else Color(0xFFFFFFFF)
                        )
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Login is needed to access this functionality",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 30.dp, vertical = 5.dp)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    if (darkTheme) Color(0xFF464646)
                                    else Color(0xFFD5D3D3)
                                )
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Username")
                            Row(
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFF949393),
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
                                    value = _un.value,
                                    modifier = Modifier.weight(1f),
                                    onValueChange = {
                                        _un.value = it
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
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFF949393),
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
                                    value = _pw.value, onValueChange = {
                                        _pw.value = it
                                        if (_pwError.value) _pwError.value = false
                                        if (_unError.value) _unError.value = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !_loggingIn.value,
                                    singleLine = true,
                                    keyboardActions = KeyboardActions(onDone = {
                                        onLogin(_un.value, _pw.value, dev.value ?: "")
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
                                    onLogin(_un.value, _pw.value, dev.value ?: "")
                                },
                                enabled = !_loggingIn.value,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(5.dp),
                                colors = ButtonDefaults.buttonColors(Color(0xFF507427))
                            ) {
                                if (_loggingIn.value)
                                    CircularLoading(modifier = Modifier.size(20.dp))
                                else Text(text = "Login")
                            }
                            Button(
                                onClick = { onCancel() },
                                enabled = !_loggingIn.value,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(5.dp),
                                colors = ButtonDefaults.buttonColors(Color.Gray)
                            ) { Text(text = "Cancel") }
                        }
                    }
                }
            }
        }
    }

    private fun onCancel() {
        _closeNow.value = true
        navigator.navigate("menu")
    }

    private fun onLogin(username: String, password: String, dev: String) {
        _loggingIn.value = true
        try {
            val api = RestAPI.create<AuthApi>(deviceId = dev)
            val call = api.login(LoginInput(username, password))
            RestAPI.execute(call, scope, onSuccess = { r ->
                _loggingIn.value = false
                scope.launch {
                    _store!!.setUser(r.user.username, r.user.oid, r.token, r.user.password)
                    _closeNow.value = true
                    onAuth()
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
}