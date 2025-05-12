package com.biztools.stockcount.ui.pages

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.biztools.stockcount.api.RestAPI
import com.biztools.stockcount.api.StockApi
import com.biztools.stockcount.models.User
import com.biztools.stockcount.ui.components.CircularLoading
import com.biztools.stockcount.ui.extensions.bestBg
import kotlinx.coroutines.CoroutineScope

@Composable
fun AddItem(
    ctx: Context,
    scope: CoroutineScope,
    user: User?,
    navigator: NavHostController,
    code: String? = null,
    fromRoute: String? = null,
    device: String = "",
    onUnauth: () -> Unit
) {
    val route = remember(fromRoute) {
        var r = fromRoute ?: "menu"
        if (r.isEmpty() || r.isBlank()) r = "menu"
        mutableStateOf(r)
    }
    val itemCode = remember {
        mutableStateOf(code ?: "")
    }
    val submitting = remember {
        mutableStateOf(false)
    }
    val invalid = remember {
        mutableStateOf(false)
    }
    val onSubmit: () -> Unit = {
        if (itemCode.value.contains(" ")) {
            invalid.value = true
        } else {
            submitting.value = true
            try {
                val api = RestAPI.create<StockApi>(user?.token, deviceId = device)
                val call = api.addItem(itemCode.value)
                RestAPI.execute(call, scope, onSuccess = {
                    navigator.navigate(route.value) { popUpTo("menu") }
                }, onError = { ex ->
                    if (ex.message?.startsWith("unauth") == true) onUnauth()
                    else Toast.makeText(ctx, ex.message, Toast.LENGTH_LONG).show()
                })
            } catch (e: Exception) {
                Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .bestBg()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(text = "Item code/number")
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = itemCode.value,
                onValueChange = {
                    itemCode.value = it
                    invalid.value = false
                },
                enabled = !submitting.value,
                singleLine = true,
                keyboardActions = KeyboardActions(onDone = { onSubmit() })
            )
            if (invalid.value) {
                Text(text = "space not allowed!", color = Color.Red)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                enabled = !submitting.value,
                colors = ButtonDefaults.buttonColors(Color.Gray),
                onClick = {
                    navigator.navigate(route.value)
                }) { Text(text = "Cancel") }
            Button(
                modifier = Modifier.weight(1f),
                enabled = !submitting.value && itemCode.value.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(Color(0xFF5B7938)),
                onClick = { onSubmit() }) {
                if (submitting.value) CircularLoading(modifier = Modifier.size(20.dp))
                else Text(text = "Submit")
            }
        }
    }
}