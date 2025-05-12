package com.biztools.stockcount.ui.layouts

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.biztools.stockcount.R
import com.biztools.stockcount.api.AuthApi
import com.biztools.stockcount.api.RestAPI
import com.biztools.stockcount.models.LoginInput
import com.biztools.stockcount.models.UiProps
import com.biztools.stockcount.models.User
import com.biztools.stockcount.stores.UserStore
import com.biztools.stockcount.ui.components.CircularLoading
import com.biztools.stockcount.ui.utilities.NoRippleInteraction
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun AuthGuard(props: UiProps, content: @Composable (p: UiProps) -> Unit) {
    val ctx = LocalContext.current
    val store = UserStore(ctx)
    val scope = rememberCoroutineScope()
    val user = store.user.collectAsState(initial = User("", "", "", "", Calendar.getInstance()))
    val focus = LocalFocusManager.current
    val showPassword = remember { mutableStateOf(false) }
    val closeNow = remember { mutableStateOf(false) }
    val un = remember { mutableStateOf("") }
    val pw = remember { mutableStateOf("") }
    val unError = remember { mutableStateOf(false) }
    val pwError = remember { mutableStateOf(false) }
    val loggingIn = remember { mutableStateOf(false) }
    val onCancel: () -> Unit = {
        closeNow.value = true
        props.navigator.navigate("menu") {
            popUpTo("menu") { inclusive = true }
        }
    }
    val onLogin: (username: String, password: String) -> Unit = { username, password ->
        loggingIn.value = true
        try {
            val api = RestAPI.create<AuthApi>()
            val call = api.login(LoginInput(username, password))
            RestAPI.execute(call, scope, onSuccess = { r ->
                loggingIn.value = false
                scope.launch {
                    store.setUser(
                        r.user.username,
                        r.user.oid,
                        r.token,
                        r.user.password
                    )
                }
            }, onError = { e ->
                unError.value = true
                pwError.value = true
                loggingIn.value = false
                Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
            })
        } catch (e: Exception) {
            unError.value = true
            pwError.value = true
            loggingIn.value = false
            Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
        }
    }
    LaunchedEffect(true) {
        if (props.drawer.isClosed) return@LaunchedEffect
        props.drawer.close()
    }
    content(
        UiProps(
            drawer = props.drawer,
            isAutoScan = props.isAutoScan,
            navigator = props.navigator,
            warehouses = props.warehouses,
            page = props.page,
            isDark = props.isDark,
            user = user.value
        )
    )
    if (user.value == null && !closeNow.value) {
        Dialog(onDismissRequest = {
            onCancel()
        }, DialogProperties()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .fillMaxWidth()
                    .background(
                        if (props.isDark) Color(0xFF222222)
                        else Color(0xFFFFFFFF)
                    )
                    .padding(20.dp),
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
                                if (props.isDark) Color(0xFF464646)
                                else Color(0xFFD5D3D3)
                            )
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Username")
                        OutlinedTextField(
                            value = un.value,
                            onValueChange = {
                                un.value = it
                                if (pwError.value) pwError.value = false
                                if (unError.value) unError.value = false
                            },
                            enabled = !loggingIn.value,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "username"
                                )
                            },
                            singleLine = true,
                            supportingText = {
                                if (unError.value) Text(
                                    text = "Invalid username",
                                    color = Color(0xFFE41616)
                                )
                            },
                            keyboardActions = KeyboardActions(onDone = {
                                focus.moveFocus(FocusDirection.Down)
                            })
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Password")
                        OutlinedTextField(
                            value = pw.value, onValueChange = {
                                pw.value = it
                                if (pwError.value) pwError.value = false
                                if (unError.value) unError.value = false
                            },
                            enabled = !loggingIn.value,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "password"
                                )
                            },
                            trailingIcon = {
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
                                                if (!loggingIn.value) showPassword.value =
                                                    !showPassword.value
                                            })
                                )
                            },
                            singleLine = true,
                            supportingText = {
                                if (pwError.value) Text(
                                    text = "Incorrect password",
                                    color = Color(0xFFE41616)
                                )
                            },
                            keyboardActions = KeyboardActions(onDone = {
                                onLogin(un.value, pw.value)
                            }),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (!showPassword.value) KeyboardType.Password
                                else KeyboardType.Text
                            ),
                            visualTransformation = if (!showPassword.value) PasswordVisualTransformation()
                            else VisualTransformation.None
                        )
                    }
                    Column {
                        Button(
                            onClick = {
                                focus.clearFocus()
                                onLogin(un.value, pw.value)
                            },
                            enabled = !loggingIn.value,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (loggingIn.value) CircularLoading(
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(text = "Login")
                            }
                        }
                        Button(
                            onClick = { onCancel() },
                            enabled = !loggingIn.value,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(5.dp)
                        ) { Text(text = "Cancel") }
                    }
                }
            }
        }
    }
}