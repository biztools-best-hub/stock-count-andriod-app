package com.biztools.stockcount.ui.pages

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.biztools.stockcount.R
import com.biztools.stockcount.api.RestAPI
import com.biztools.stockcount.api.StockApi
import com.biztools.stockcount.models.AddItemsInput
import com.biztools.stockcount.models.PrintBarcodesInput
import com.biztools.stockcount.models.PrintBarcodesResult
import com.biztools.stockcount.models.RateCard
import com.biztools.stockcount.models.User
import com.biztools.stockcount.ui.components.CircularLoading
import com.biztools.stockcount.ui.extensions.bestBg
import com.biztools.stockcount.ui.extensions.innerShadow
import com.biztools.stockcount.ui.theme.BlackBg
import com.biztools.stockcount.ui.theme.White100
import com.biztools.stockcount.ui.utilities.NoRippleInteraction
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope

enum class ReprintMode {
    NONE, ALL, ONLY
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PrintLabel(
    ctx: Context,
    scope: CoroutineScope,
    isDark: Boolean,
    user: User?,
    device: String = "",
    onUnauth: () -> Unit
) {
    val barcodes = remember { mutableListOf<String>() }
    val currentBarcode = remember { mutableStateOf<String?>(null) }
    val currentRate = remember { mutableStateOf<RateCard?>(null) }
    val initializing = remember { mutableStateOf(false) }
    val rates = remember { mutableStateOf<List<RateCard>?>(null) }
    val cameraPermission = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val canShowCamera = remember { mutableStateOf(false) }
    val imageCapture = remember { mutableStateOf<Bitmap?>(null) }
    val format = remember { mutableStateOf<Int?>(null) }
    val printing = remember { mutableStateOf(false) }
    val expanded = remember { mutableStateOf(false) }
    val hasError = remember { mutableStateOf(false) }
    val yesPrintingThem = remember { mutableStateOf(false) }
    val yesPrintingAll = remember { mutableStateOf(false) }
    val yesAdding = remember { mutableStateOf(false) }
    val printResult = remember {
        mutableStateOf<PrintBarcodesResult?>(null)
    }
    val keyboardCtl = LocalSoftwareKeyboardController.current
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
    val getRates: () -> Unit = {
        if (rates.value == null) {
            try {
                val ratesApi = RestAPI.create<StockApi>(user?.token, deviceId = device)
                val call = ratesApi.getRateCards()
                RestAPI.execute(call, scope, onSuccess = { r ->
                    rates.value = r
                    initializing.value = false
                }, onError = { e ->
                    if (e.message?.startsWith("unauth") == true) onUnauth()
                    else Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
                })
            } catch (e: Exception) {
                Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
    val printBarcodes: () -> Unit = {
        printResult.value = null
        try {
            printing.value = true
            val printApi = RestAPI.create<StockApi>(user?.token, deviceId = device)
            val printCall = printApi
                .printBatchBarcodes(PrintBarcodesInput(currentRate.value?.oid, barcodes))
            RestAPI.execute(printCall, scope,
                onSuccess = { r ->
                    printResult.value = r
                    printing.value = false
                    if (r.notFoundCodes.isNotEmpty()) {
                        printing.value = false
                        hasError.value = true
                    } else Toast.makeText(ctx, "printing", Toast.LENGTH_LONG).show()
                },
                onError = { e ->
                    printing.value = false
                    if (e.message?.startsWith("unauth") == true) onUnauth()
                    else Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
                })
        } catch (e: Exception) {
            printing.value = false
            Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
        }
    }
    val addItems: (mode: ReprintMode) -> Unit = { m ->
        try {
            when (m) {
                ReprintMode.NONE -> yesAdding.value = true
                ReprintMode.ALL -> yesPrintingAll.value = true
                else -> yesPrintingThem.value = true
            }
            val addApi = RestAPI.create<StockApi>(user?.token, deviceId = device)
            val addCall = addApi.addItems(AddItemsInput(printResult.value!!.notFoundCodes))
            RestAPI.execute(addCall, scope, onSuccess = {
                Toast.makeText(ctx, "Items added", Toast.LENGTH_SHORT).show()
                if (m != ReprintMode.NONE) {
                    val codes = when (m) {
                        ReprintMode.ONLY -> printResult.value!!.notFoundCodes
                        else -> barcodes
                    }
                    try {
                        val printCall = addApi.printBatchBarcodes(
                            PrintBarcodesInput(
                                rateCardOid = currentRate.value?.oid,
                                barCodes = codes.toMutableList()
                            )
                        )
                        RestAPI.execute(printCall, scope, onSuccess = {
                            yesAdding.value = false
                            yesPrintingAll.value = false
                            yesPrintingThem.value = false
                            hasError.value = false
                            Toast.makeText(ctx, "printing", Toast.LENGTH_LONG).show()
                        }, onError = { exc ->
                            yesAdding.value = false
                            yesPrintingAll.value = false
                            yesPrintingThem.value = false
                            if (exc.message?.startsWith("unauth") == true) onUnauth()
                            else Toast.makeText(ctx, exc.message, Toast.LENGTH_LONG).show()
                        })
                    } catch (exception: Exception) {
                        yesAdding.value = false
                        yesPrintingAll.value = false
                        yesPrintingThem.value = false
                        Toast.makeText(ctx, exception.message, Toast.LENGTH_LONG).show()
                    }
                } else {
                    yesAdding.value = false
                    yesPrintingAll.value = false
                    yesPrintingThem.value = false
                    hasError.value = false
                }
            }, onError = { ex ->
                yesAdding.value = false
                yesPrintingAll.value = false
                yesPrintingThem.value = false
                if (ex.message?.startsWith("unauth") == true) onUnauth()
                else Toast.makeText(ctx, ex.message, Toast.LENGTH_LONG).show()
            })
        } catch (e: Exception) {
            yesAdding.value = false
            yesPrintingAll.value = false
            yesPrintingThem.value = false
            Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
        }
    }
    BackHandler(canShowCamera.value) {
        canShowCamera.value = false
    }
    LaunchedEffect(density.density) {
        snapshotFlow {
            ime.getBottom(density) - navbar.getBottom(density)
        }.collect {
            val h = (it / density.density).dp
            kHeight.value = maxOf(h, kHeight.value)
            isKeyboardOpened.value = h >= kHeight.value
        }
    }

    LaunchedEffect(isKeyboardOpened.value) {
        if (isKeyboardOpened.value && firstFocus.value) {
            keyboardCtrl?.hide()
            firstFocus.value = false
        }
    }
    LaunchedEffect(user, rates.value) { if (user != null && rates.value == null) getRates() }
    if (initializing.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularLoading(Modifier.size(80.dp))
        }
    } else if (canShowCamera.value) {
        LaunchedEffect(canShowCamera.value, cameraPermission.hasPermission) {
            if (canShowCamera.value && !cameraPermission.hasPermission) cameraPermission.launchPermissionRequest()
        }
        if (cameraPermission.hasPermission) {
            CameraBox(ctx, scope, onCodeDetected = { code ->
//                currentBarcode.value = code
                barcodes.add(code)
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
        Modifier
            .fillMaxSize()
            .bestBg(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            Modifier
                .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "Current barcode")
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
                LaunchedEffect(true) {
                    focusRequest.requestFocus()
                    focusRequest.captureFocus()
                }
                BasicTextField(
                    enabled = !printing.value,
                    singleLine = true,
                    value = currentBarcode.value ?: "",
                    onValueChange = { currentBarcode.value = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequest),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardCtl?.hide()
                        barcodes.add(currentBarcode.value!!)
                        currentBarcode.value = null
                        imageCapture.value = null
                    })
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "ok",
                    modifier = Modifier
                        .clickable(
                            interactionSource = NoRippleInteraction(),
                            indication = null,
                            enabled = !printing.value && currentBarcode.value != null,
                            onClick = {
                                keyboardCtl?.hide()
                                barcodes.add(currentBarcode.value!!)
                                currentBarcode.value = null
                                imageCapture.value = null
                            })
                        .clip(CircleShape)
                        .size(16.dp)
                        .background(
                            if (printing.value || currentBarcode.value == null)
                                Color(0x23B8F0E3) else Color(0xFF86CABA)
                        )
                        .padding(2.dp)
                )
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = "Barcodes batch",
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .background(Color(0xFFE2E2E2))
                    .innerShadow(
                        blur = 5.dp,
                        color = if (isDark) Color.Black else Color(0xC87E7E7E)
                    )
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(count = barcodes.size, key = { idx -> "${barcodes[idx]}-${idx}" }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = barcodes[it])
                        Icon(
                            painter = painterResource(id = R.drawable.trash_bin),
                            contentDescription = "remove",
                            tint = Color(0xFFAC3A3A),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    if (currentBarcode.value == barcodes[it]) {
                                        currentBarcode.value = null
                                    }
                                    barcodes.removeAt(it)
                                }
                        )
                    }
                }
            }
        }
        Column(
            Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "Rate card")
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(5.dp))
                        .fillMaxWidth()
                        .clickable {
                            expanded.value = !expanded.value
                        }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = currentRate.value?.name ?: "Select rate card")
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "select-rate"
                    )
                }
                DropdownMenu(
                    expanded = expanded.value,
                    modifier = Modifier.width((LocalConfiguration.current.screenWidthDp - 10).dp),
                    onDismissRequest = { expanded.value = false }) {
                    if (rates.value != null) {
                        repeat(rates.value!!.size) {
                            DropdownMenuItem(
                                text = { Text(text = rates.value!![it].name) },
                                onClick = {
                                    currentRate.value = rates.value!![it]
                                    expanded.value = false
                                })
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                .fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = {
                    barcodes.clear()
                    currentBarcode.value = null
                }, modifier = Modifier
                    .weight(1f),
                enabled = barcodes.isNotEmpty() && !printing.value,
                colors = ButtonDefaults.buttonColors(Color.Gray)
            ) { Text(text = "Clear") }
            Button(
                onClick = { printBarcodes() },
                enabled = barcodes.isNotEmpty() && !printing.value,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(Color(0xFF5C7A38))
            ) {
                if (printing.value) {
                    CircularLoading(modifier = Modifier.size(10.dp))
                } else Text(text = "Print")
            }
            Button(
                onClick = { canShowCamera.value = true }, modifier = Modifier
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(Color(0xFF25608F)),
                enabled = !printing.value
            ) { Text(text = "Scan") }
        }
    }
    if (hasError.value) Dialog(onDismissRequest = { hasError.value = false }) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDark) BlackBg else White100)
                    .padding(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "warning",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFFFF5722)
                    )
                    if (printResult.value!!.notFoundCodes.size < barcodes.size) {
                        Text(text = "Item labels are printed", textAlign = TextAlign.Center)
                    }
                    Text(
                        text = "Can't find items of codes below",
                        textAlign = TextAlign.Center,
                    )
                    LazyColumn {
                        items(
                            printResult.value!!.notFoundCodes.size,
                            key = { printResult.value!!.notFoundCodes[it] }) {
                            Text(text = printResult.value!!.notFoundCodes[it])
                        }
                    }
                    Text(text = "Add them as new items?", fontWeight = FontWeight.Bold)
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(Color(0xFF5B7C34)),
                            onClick = { addItems(ReprintMode.ONLY) }) { Text(text = "Yes and print them") }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(Color(0xFF5B7C34)),
                            onClick = { addItems(ReprintMode.ALL) }) { Text(text = "Yes and print all again") }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(Color(0xFF5B7C34)),
                            onClick = { addItems(ReprintMode.NONE) }) { Text(text = "Yes") }
                        Button(
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(Color.Gray),
                            onClick = { hasError.value = false }) { Text(text = "No") }
                    }
                }
            }
        }
    }
}