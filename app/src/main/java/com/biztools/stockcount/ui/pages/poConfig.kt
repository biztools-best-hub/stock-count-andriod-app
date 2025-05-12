package com.biztools.stockcount.ui.pages

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.biztools.stockcount.R
import com.biztools.stockcount.models.POConfiguration
import com.biztools.stockcount.stores.POConfigStore
import com.biztools.stockcount.ui.components.CircularLoading
import com.biztools.stockcount.ui.extensions.bestBg
import com.biztools.stockcount.ui.utilities.NoRippleInteraction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun POConfig(ctx: Context, scope: CoroutineScope) {
    val initializing = remember { mutableStateOf(true) }
    val configStore = POConfigStore(ctx)
    val showSuccess = remember { mutableStateOf(false) }
    val config = configStore.config.collectAsState(initial = null).value
    val wh = remember { mutableStateOf("") }
    val s1 = remember { mutableStateOf("") }
    val s2 = remember { mutableStateOf("") }
    val s4 = remember { mutableStateOf("") }
    val s5 = remember { mutableStateOf("") }
    val s6 = remember { mutableStateOf("") }
    val s7 = remember { mutableStateOf("") }
    val s8 = remember { mutableStateOf("") }
    val s9 = remember { mutableStateOf("") }
    val s10 = remember { mutableStateOf("") }
    val onClear: () -> Unit = {
        wh.value = ""
        s1.value = ""
        s2.value = ""
        s4.value = ""
        s5.value = ""
        s6.value = ""
        s7.value = ""
        s8.value = ""
        s9.value = ""
        s10.value = ""
    }
    val onSave: () -> Unit = {
        scope.launch {
            if (listOf(
                    wh.value,
                    s1.value,
                    s2.value,
                    s4.value,
                    s5.value,
                    s6.value,
                    s7.value,
                    s8.value,
                    s9.value,
                    s10.value
                ).all { v -> v.isEmpty() }
            ) configStore.clear()
            else configStore.setConfig(
                POConfiguration(
                    warehouse = wh.value,
                    s1 = s1.value,
                    s2 = s2.value,
                    s4 = s4.value,
                    s5 = s5.value,
                    s6 = s6.value,
                    s7 = s7.value,
                    s8 = s8.value,
                    s9 = s9.value,
                    s10 = s10.value
                )
            )
            showSuccess.value = true
        }
    }
    LaunchedEffect(config) {
        if (config != null) {
            wh.value = config.warehouse
            s1.value = config.s1
            s2.value = config.s2
            s4.value = config.s4
            s5.value = config.s5
            s6.value = config.s6
            s7.value = config.s7
            s8.value = config.s8
            s9.value = config.s9
            s10.value = config.s10
            initializing.value = false
        }
    }
    if (initializing.value) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .bestBg(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) { CircularLoading(Modifier.width(30.dp)) }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Initializing...")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .bestBg(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val focusManager = LocalFocusManager.current
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val list = listOf(
                    wh,
                    s1,
                    s2,
                    s4,
                    s5,
                    s6,
                    s7,
                    s8,
                    s9,
                    s10
                )
                items(count = list.size, key = { idx -> idx }) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = if (it < 1) "Warehouse" else "Segment ${if (it < 3) it else it + 1}")
                        Row(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF919191),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            BasicTextField(
                                modifier = Modifier.weight(1f),
                                value = list[it].value,
                                singleLine = true,
                                onValueChange = { v -> list[it].value = v },
                                keyboardActions = KeyboardActions(onDone = {
                                    if (it < list.size - 1) focusManager.moveFocus(FocusDirection.Down)
                                    else focusManager.clearFocus()
                                })
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = NoRippleInteraction(),
                                        enabled = list[it].value.isNotEmpty(),
                                        indication = null,
                                        onClick = { list[it].value = "" })
                                    .background(if (list[it].value.isNotEmpty()) Color(0xFF6F6F6F) else Color.White)
                                    .size(20.dp), contentAlignment = Alignment.Center
                            ) {
                                if (list[it].value.isNotEmpty()) Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "${if (it < 1) "warehouse" else "s${if (it < 3) it else it + 1}"}-clear",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onClear()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "clear",
                            modifier = Modifier.width(20.dp)
                        )
                        Text(text = "Clear")
                    }
                }
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSave()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF144B8F))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.save),
                            contentDescription = "save",
                            modifier = Modifier.width(20.dp)
                        )
                        Text(text = "Save")
                    }
                }
            }
        }
    }
    if (showSuccess.value) Dialog(onDismissRequest = { showSuccess.value = false }) {
        Card {
            Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "success",
                        modifier = Modifier.size(60.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Text(text = "PO config is saved")
                    Button(onClick = { showSuccess.value = false }) {
                        Text(text = "OK")
                    }
                }
            }
        }
    }
}