package com.biztools.stockcount.ui.pages

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.biztools.stockcount.R
import com.biztools.stockcount.api.RestAPI
import com.biztools.stockcount.api.StockApi
import com.biztools.stockcount.models.ItemInfo
import com.biztools.stockcount.models.StockOrder
import com.biztools.stockcount.models.StockOrderInput
import com.biztools.stockcount.models.User
import com.biztools.stockcount.stores.POConfigStore
import com.biztools.stockcount.stores.SecurityStore
import com.biztools.stockcount.ui.components.CircularLoading
import com.biztools.stockcount.ui.extensions.bestBg
import com.biztools.stockcount.ui.extensions.customShadow
import com.biztools.stockcount.ui.extensions.innerShadow
import com.biztools.stockcount.ui.utilities.NoRippleInteraction
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope

data class ExtendItemInfo(
    val info: ItemInfo,
    var selected: Boolean = false,
    var qty: Int
)

enum class ConfirmMode {
    CLEAR,
    ADD_STOCK
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalComposeUiApi::class)
@Composable
fun Po(
    ctx: Context,
    scope: CoroutineScope,
    isDark: Boolean,
    user: User?,
    page: String,
    navigator: NavHostController,
    onUnauth: () -> Unit
) {
    val config = POConfigStore(ctx).config.collectAsState(initial = null).value
    val device = SecurityStore(ctx).device.collectAsState(initial = null).value
    val initializing = remember { mutableStateOf(true) }
    val barcode = remember { mutableStateOf<String?>(null) }
    val items = remember { mutableListOf<ExtendItemInfo>() }
    val itemInfo = remember { mutableStateOf<ItemInfo?>(null) }
    val checking = remember { mutableStateOf(false) }
    val errorMsg = remember { mutableStateOf<String?>(null) }
    val showItem = remember { mutableStateOf(false) }
    val showConfirm = remember { mutableStateOf(false) }
    val confirmMode = remember { mutableStateOf(ConfirmMode.ADD_STOCK) }
    val cameraPermission = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val canShowCamera = remember { mutableStateOf(false) }
    val imageCapture = remember { mutableStateOf<Bitmap?>(null) }
    val format = remember { mutableStateOf<Int?>(null) }
    val hasItemError = remember { mutableStateOf(false) }
    val showEdit = remember { mutableStateOf(false) }
    val hasError = remember { mutableStateOf(false) }
    val invalidQty = remember { mutableStateOf(false) }
    val submitting = remember { mutableStateOf(false) }
    val showSubmit = remember { mutableStateOf(false) }
    val qty = remember { mutableStateOf<Int?>(1) }
    val selectedItemQty = remember { mutableStateOf<Int?>(1) }
    val firstFocus = remember { mutableStateOf(true) }
    val ime = WindowInsets.ime
    val navbar = WindowInsets.navigationBars
    val density = LocalDensity.current
    val kHeight = remember {
        mutableStateOf(0.dp)
    }
    val keyboardCtrl = LocalSoftwareKeyboardController.current
    val isKeyboardOpened = remember {
        mutableStateOf(false)
    }
    val focusRequest = FocusRequester()
    val onAddToList: () -> Unit = {
        if (qty.value == null || qty.value!! < 1) invalidQty.value = true
        else {
            if (items.isEmpty() || items.all { v -> v.info.oid != itemInfo.value!!.oid }) {
                items.add(ExtendItemInfo(info = itemInfo.value!!, qty = qty.value!!))
            } else {
                val idx = items.indexOfFirst { v -> v.info.oid == itemInfo.value!!.oid }
                items[idx].qty += qty.value!!
            }
            itemInfo.value = null
            barcode.value = null
            qty.value = 1
            showItem.value = false
            imageCapture.value = null
        }
    }
    val checkItem: () -> Unit = {
        itemInfo.value = null
        qty.value = 1
        val invalid =
            barcode.value.isNullOrEmpty() || barcode.value.isNullOrBlank() || config?.warehouse == null
        if (!invalid) {
            checking.value = true
            showItem.value = true
            try {
                val api = RestAPI.create<StockApi>(user!!.token, deviceId = device ?: "")
                val call = api.checkItemInfoNonStrict(barcode.value!!)
                RestAPI.execute(call, scope,
                    onSuccess = { r ->
                        itemInfo.value = r
                        checking.value = false
                        barcode.value = ""
                    },
                    onError = { e ->
                        if (e.message?.startsWith("unauth") == true) onUnauth()
                        else if (e.message.isNullOrEmpty() || e.message.isNullOrBlank() || e.message!!.lowercase() != "not found") {
                            hasItemError.value = true
                        }
                        errorMsg.value = e.message
                        checking.value = false
                    })
            } catch (e: Exception) {
                hasItemError.value = true
                errorMsg.value = e.message
                checking.value = false
            }
        }
    }
    val onSubmit: () -> Unit = {
        if (items.size > 0) {
            submitting.value = true
            showSubmit.value = true
            try {
                val submitApi = RestAPI.create<StockApi>(token = user!!.token)
                val submitCall = submitApi.createPO(StockOrderInput(orders = items.map { v ->
                    StockOrder(
                        itemOid = v.info.oid,
                        itemNumber = v.info.number,
                        warehouse = config!!.warehouse,
                        qty = v.qty,
                        s1 = config.s1,
                        s2 = config.s2,
                        s4 = config.s4,
                        s5 = config.s5,
                        s6 = config.s6,
                        s7 = config.s7,
                        s8 = config.s8,
                        s9 = config.s9,
                        s10 = config.s10,
                    )
                }.toMutableList()))
                RestAPI.execute(submitCall, scope,
                    onSuccess = {
                        submitting.value = false
                        items.clear()
                    },
                    onError = { ex ->
                        hasError.value = true
                        submitting.value = false
                        errorMsg.value = ex.message
                        if (ex.message?.startsWith("unauth") == true) onUnauth()
                        else Toast.makeText(ctx, ex.message, Toast.LENGTH_LONG).show()
                    })
            } catch (e: Exception) {
                hasError.value = true
                submitting.value = false
                errorMsg.value = e.message
                Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
    LaunchedEffect(config) {
        if (config != null) initializing.value = false
    }
    BackHandler(showConfirm.value) {
        showConfirm.value = false
    }
    BackHandler(showEdit.value) {
        showEdit.value = false
    }
    BackHandler(showItem.value) {
        showItem.value = false
    }
    BackHandler(canShowCamera.value) {
        canShowCamera.value = false
        if (imageCapture.value != null) imageCapture.value = null
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    if (initializing.value) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) { CircularLoading(Modifier.width(30.dp)) }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Initializing...")
        }
    } else {
        if (canShowCamera.value) {
            LaunchedEffect(canShowCamera.value, cameraPermission.hasPermission) {
                if (canShowCamera.value && !cameraPermission.hasPermission) cameraPermission.launchPermissionRequest()
            }
            if (cameraPermission.hasPermission) {
                CameraBox(ctx, scope, onCodeDetected = { code ->
                    barcode.value = code
                    checkItem()
                    canShowCamera.value = false
                }, onStartAnalyze = { img, fm ->
                    try {
                        imageCapture.value = img
                    } catch (e: Exception) {
                        Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
                    }
                    format.value = fm
                })
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Need camera permission")
                    Button(onClick = { canShowCamera.value = false }) { Text(text = "Cancel") }
                }
            }
        } else Column(
            modifier = Modifier
                .fillMaxSize()
                .bestBg(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Column(
                Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(text = "Item Number")
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
                    LaunchedEffect(density.density) {
                        snapshotFlow {
                            ime.getBottom(density) - navbar.getBottom(density)
                        }.collect {
                            val h = (it / density.density).dp
                            kHeight.value = maxOf(h, kHeight.value)
                            isKeyboardOpened.value = h >= kHeight.value
                        }
                    }
                    LaunchedEffect(Unit) {
                        focusRequest.requestFocus()
                        focusRequest.captureFocus()
                    }
                    LaunchedEffect(isKeyboardOpened.value) {
                        if (isKeyboardOpened.value && firstFocus.value) {
                            keyboardCtrl?.hide()
                            firstFocus.value = false
                        }
                    }
                    BasicTextField(
                        value = barcode.value ?: "",
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequest),
                        onValueChange = { barcode.value = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            checkItem()
                        }),
                        enabled = config?.warehouse != null
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.scan),
                        contentDescription = "scan",
                        Modifier
                            .size(20.dp)
                            .clickable(
                                interactionSource = NoRippleInteraction(),
                                null,
                                onClick = {
                                    if (config?.warehouse != null) {
                                        keyboardController?.hide()
                                        canShowCamera.value = true
                                    }
                                })
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .innerShadow(blur = 3.dp, color = Color(0xA6000000))
                    .background(Color(0xFFE2E2E2))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(count = items.size, key = { idx -> items[idx].info.oid }) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                keyboardController?.hide()
                                selectedItemQty.value = items[it].qty
                                for (itm in items) {
                                    if (itm.info.oid == items[it].info.oid) {
                                        itm.selected = !itm.selected
                                    } else itm.selected = false
                                }
                            }
                            .fillMaxWidth()
                            .background(
                                if (items[it].selected) Color(0xFF4AAEFD) else Color.White
                            )
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = items[it].info.number)
                        Text(text = items[it].qty.toString())
                    }
                }
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = {
                        keyboardController?.hide()
                        showEdit.value = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(Color(0xFF205F91)),
                    enabled = items.any { v -> v.selected }) {
                    Text(text = "View/Edit", fontSize = 11.sp)
                }
                Button(
                    onClick = {
                        keyboardController?.hide()
                        val idx = items.indexOfFirst { v -> v.selected }
                        items.removeAt(idx)
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFFB63636)),
                    modifier = Modifier.weight(1f),
                    enabled = items.any { v -> v.selected }) {
                    Text(
                        text = "Delete",
                        fontSize = 10.sp
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = {
                        keyboardController?.hide()
                        confirmMode.value = ConfirmMode.CLEAR
                        showConfirm.value = true
                    },
                    enabled = items.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(Color.Gray),
                    modifier = Modifier.weight(1f)
                ) { Text(text = "Clear", fontSize = 10.sp) }

                Button(
                    onClick = {
                        keyboardController?.hide()
                        confirmMode.value = ConfirmMode.ADD_STOCK
                        showConfirm.value = true
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFF5E8332)),
                    enabled = items.size > 0,
                    modifier = Modifier.weight(1f)
                ) { Text(text = "Submit", fontSize = 10.sp) }
            }
        }
    }
    if (showItem.value) Dialog(
        onDismissRequest = {
            showItem.value = false
            if (invalidQty.value) invalidQty.value = false
        },
        DialogProperties(dismissOnClickOutside = false)
    ) {
        Card {
            Box(
                modifier = Modifier.padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (imageCapture.value != null) Box(
                        modifier = Modifier
                            .customShadow(alpha = .8f, blur = 3f, corner = 4.dp.value)
                            .clip(RoundedCornerShape(4.dp))
                            .width(160.dp)
                            .height(120.dp)
                    ) {
                        Image(
                            bitmap = imageCapture.value!!.asImageBitmap(),
                            contentDescription = "barcode-image",
                            modifier = Modifier
                                .clipToBounds()
                                .scale(2f)
                                .rotate(90f)
                        )
                    }
                    if (checking.value) Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularLoading()
                        Text(text = "Checking...")
                    }
                    else {
                        if (itemInfo.value == null || itemInfo.value!!.supplier.isBlank() || itemInfo.value!!.supplier.isEmpty()) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (itemInfo.value == null) {
                                    if (hasItemError.value) {
                                        Text(
                                            text = errorMsg.value ?: "some thing went wrong"
                                        )
                                        Button(
                                            onClick = {
                                                barcode.value = null
                                                showItem.value = false
                                            },
                                            colors = ButtonDefaults.buttonColors(Color(0xFF1F5E91))
                                        ) { Text(text = "OK") }
                                    } else {
                                        if (imageCapture.value == null) Box(
                                            modifier = Modifier
                                                .width(160.dp)
                                                .height(120.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.no_codes),
                                                contentDescription = "not-found",
                                            )
                                        }
                                        Text(text = "Item not found")
                                        Text(
                                            text = "Do you want to add new item?",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    Color(0xFF56772F)
                                                ),
                                                onClick = {
                                                    navigator.navigate("add-item?code=${barcode.value}&fromRoute=${page}")
                                                    showItem.value = false
                                                }) {
                                                Text(text = "yes")
                                            }
                                            Button(
                                                modifier = Modifier.weight(1f),
                                                onClick = {
                                                    barcode.value = null
                                                    showItem.value = false
                                                },
                                                colors = ButtonDefaults.buttonColors(Color.Gray),
                                            ) { Text(text = "no") }
                                        }
                                    }
                                } else if (itemInfo.value!!.supplier.isBlank() || itemInfo.value!!.supplier.isEmpty()) {
                                    Text(
                                        text = "Item has no supplier, cannot create purchase order",
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = {
                                            barcode.value = null
                                            showItem.value = false
                                        },
                                        colors = ButtonDefaults.buttonColors(Color(0xFF215985))
                                    ) { Text(text = "OK") }
                                }
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = "Item Number")
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = 1.dp,
                                                color = if (isDark) Color(0xFF363636)
                                                else Color(0xFFBDBDBD),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(8.dp)
                                    ) { Text(text = itemInfo.value!!.number) }
                                }
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = "Item Name")
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = 1.dp,
                                                color = if (isDark) Color(0xFF363636)
                                                else Color(0xFFBDBDBD),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(8.dp)
                                    ) { Text(text = itemInfo.value!!.name) }
                                }
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = "Put quantity")
                                    BasicTextField(
                                        modifier = Modifier
                                            .border(
                                                width = 1.dp,
                                                color = Color(0xFFB1B0B0),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        value = qty.value?.toString() ?: "",
                                        onValueChange = {
                                            if (invalidQty.value) invalidQty.value = false
                                            qty.value = it.toIntOrNull()
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        keyboardActions = KeyboardActions(onDone = { onAddToList() })
                                    )
                                    if (invalidQty.value) Text(
                                        text = "Quantity to order must not be less than 1",
                                        color = Color(0xFFB92E2E),
                                        fontSize = 10.sp
                                    )
                                }
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(Color.Gray),
                                        onClick = {
                                            showItem.value = false
                                            barcode.value = null
                                            imageCapture.value = null
                                            qty.value = 1
                                            if (invalidQty.value) invalidQty.value = false
                                        }) { Text(text = "Cancel") }
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(Color(0xFF1F5E91)),
                                        onClick = { onAddToList() }) {
                                        Text(text = "Add to list")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showConfirm.value) Dialog(
        onDismissRequest = { showConfirm.value = false },
        DialogProperties(dismissOnClickOutside = false)
    ) {
        Card {
            Box(
                modifier = Modifier.padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Are you sure?", fontSize = 16.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showConfirm.value = false },
                            modifier = Modifier.weight(1f)
                        ) { Text(text = "No") }
                        Button(
                            onClick = {
                                if (confirmMode.value == ConfirmMode.CLEAR) {
                                    items.clear()
                                    showConfirm.value = false
                                } else {
                                    showConfirm.value = false
                                    onSubmit()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text(text = "Yes") }
                    }
                }
            }
        }
    }
    if (showEdit.value && items.any { v -> v.selected }) Dialog(
        onDismissRequest = {
            showEdit.value = false
            if (invalidQty.value) invalidQty.value = false
        },
        DialogProperties(dismissOnClickOutside = false)
    ) {
        val idx = items.indexOfFirst { v -> v.selected }
        if (idx >= 0) Card {
            Box(
                modifier = Modifier.padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Item Number")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (isDark) Color(0xFF363636)
                                    else Color(0xFFBDBDBD),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(10.dp)
                        ) { Text(text = items[idx].info.number) }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Item Name")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (isDark) Color(0xFF363636)
                                    else Color(0xFFBDBDBD),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(8.dp)
                        ) { Text(text = items[idx].info.name) }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Put quantity")
                        BasicTextField(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFA3A3A3),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .fillMaxWidth()
                                .padding(8.dp),
                            value = selectedItemQty.value?.toString() ?: "",
                            onValueChange = {
                                if (invalidQty.value) invalidQty.value = false
                                selectedItemQty.value = it.toIntOrNull()
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            keyboardActions = KeyboardActions(onDone = {
                                selectedItemQty.value = items[idx].qty
                                if (invalidQty.value) invalidQty.value = false
                                showEdit.value = false
                            })
                        )
                        if (invalidQty.value) Text(
                            text = "Quantity to order must not be less than 1",
                            color = Color(0xFFB92E2E),
                            fontSize = 10.sp
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedItemQty.value = items[idx].qty
                                if (invalidQty.value) invalidQty.value = false
                                showEdit.value = false
                            }, colors = ButtonDefaults.buttonColors(Color.Gray)
                        ) { Text(text = "Cancel") }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (invalidQty.value) invalidQty.value = false
                                items[idx].qty = selectedItemQty.value!!
                                showEdit.value = false
                            }, colors = ButtonDefaults.buttonColors(Color(0xFF5A7936))
                        ) { Text(text = "Save") }
                    }
                }
            }
        }
    }
    if (showSubmit.value) Dialog(
        onDismissRequest = { showSubmit.value = false },
        DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
    ) {
        Card {
            Box(
                modifier = Modifier.padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (submitting.value) {
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularLoading(Modifier.width(60.dp)) }
                        Text(text = "Submitting...")
                    } else if (hasError.value) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "wrong",
                            tint = Color(0xFFE64000),
                            modifier = Modifier.size(30.dp)
                        )
                        Text(text = errorMsg.value ?: "Something went wrong!")
                        Button(onClick = { showSubmit.value = false }) { Text(text = "OK") }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "success",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(60.dp)
                        )
                        Text(
                            text = "Success",
                            modifier = Modifier.width(160.dp),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { showSubmit.value = false },
                            colors = ButtonDefaults.buttonColors(
                                Color(0xFF2B3C9B)
                            )
                        ) { Text(text = "OK") }
                    }
                }
            }
        }
    }
}