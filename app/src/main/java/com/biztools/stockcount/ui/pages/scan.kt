package com.biztools.stockcount.ui.pages

import android.widget.Toast
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.biztools.stockcount.R
import com.biztools.stockcount.api.RestAPI
import com.biztools.stockcount.api.StockApi
import com.biztools.stockcount.models.ItemOnHandResult
import com.biztools.stockcount.models.OnHandItem
import com.biztools.stockcount.presentations.pagePresentations.ScanPresenter
import com.biztools.stockcount.ui.components.CircularLoading
import com.biztools.stockcount.ui.extensions.bestBg
import com.biztools.stockcount.ui.extensions.customShadow

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@ExperimentalGetImage
fun Scan(presenter: ScanPresenter) {
    LaunchedEffect(Unit) {
        if (presenter.cameraGranted) return@LaunchedEffect
        presenter.requestPermission()
    }
    var isChecking by remember { mutableStateOf(false) }
    var checkByScroll by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedNumber by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val reachedBottom: Boolean by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= 10 && lastVisibleItem.index + 1 > listState.layoutInfo.totalItemsCount - 1
        }
    }
    var onHandResult by remember { mutableStateOf<ItemOnHandResult?>(null) }
    val reservedItems = remember { mutableListOf<OnHandItem>() }
    var currentPage by remember { mutableIntStateOf(0) }
    var prevPage by remember { mutableIntStateOf(0) }
    val externalCode = remember {
        mutableStateOf("")
    }
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
    val externalCount = remember {
        mutableStateOf("1")
    }
    val externalAdded = remember {
        mutableStateOf(false)
    }

    fun onSuccessCall(res: ItemOnHandResult) {
        onHandResult = res
        isChecking = false
        if (res.items.isNotEmpty()) {
            reservedItems.addAll(res.items.filter { reservedItems.all { r -> r.number != it.number } })
            prevPage = currentPage
            if (!checkByScroll) showDialog = true
        } else if (!checkByScroll) showDialog = false
    }

    fun onSearchOffline(barcode: String) {
        try {
            if (barcode.isEmpty() || barcode.isBlank()) return
            selectedNumber = ""
            isChecking = true
            if (onHandResult != null) {
                val tempResult = onHandResult
                tempResult!!.number = ""
                onHandResult = tempResult
            }
            reservedItems.clear()
            val items = presenter.searchItemsOffline(barcode)
            if (items.isEmpty()) throw Exception("item not found")
            val r = ItemOnHandResult(
                number = "",
                name = "",
                oid = "",
                description2 = "",
                description = "",
                price = 0.0,
                brand = "",
                group = "",
                pdtCat = "",
                subCat1 = "",
                subCat2 = "",
                subCat3 = "",
                warehouses = listOf(),
                totalSOH = 0.0,
                wholeSalePrice = 0.0,
                unitCost = 0.0,
                items = presenter.searchItemsOffline(barcode)
            )
            isChecking = false
            onSuccessCall(r)
        } catch (e: Exception) {
            Toast.makeText(presenter.ctx, e.message, Toast.LENGTH_LONG)
                .show()
            isChecking = false
        }
//        if (currentPage < 2) reservedItems.clear()
    }

    fun onSearch(barcode: String) {
        if (barcode.isEmpty() || barcode.isBlank()) return
        selectedNumber = ""
        isChecking = true
        if (onHandResult != null) {
            val tempResult = onHandResult
            tempResult!!.number = ""
            onHandResult = tempResult
        }
        if (currentPage < 2) reservedItems.clear()
//        if (checkPromoResult != null) {
//            val tempPromo = checkPromoResult
//            tempPromo!!.number = ""
//            checkPromoResult = tempPromo
//        }
        try {
            val searchApi = RestAPI.create<StockApi>()
            val searchCall = searchApi.searchItems(barcode, currentPage, 20)
            RestAPI.execute(
                searchCall,
                presenter.scope!!,
                onSuccess = { res -> onSuccessCall(res) },
                onError = { e ->
                    Toast.makeText(presenter.ctx, e.message, Toast.LENGTH_LONG).show()
                    isChecking = false
                })
        } catch (e: Exception) {
            Toast.makeText(presenter.ctx, e.message, Toast.LENGTH_LONG)
                .show()
            isChecking = false
        }
    }
    LaunchedEffect(reservedItems.size) {
        if (reservedItems.size > 0 && checkByScroll) {
            listState.animateScrollBy(60f)
        }
    }

    LaunchedEffect(reachedBottom) {
        if (reachedBottom) {
            currentPage += 1
            checkByScroll = true
            if (presenter.offline) {
                onSearchOffline(externalCode.value)
            } else {
                onSearch(externalCode.value)
            }
        }
    }
    LaunchedEffect(showDialog) {
        if (!showDialog && onHandResult != null) {
            listState.scrollToItem(0)
            onHandResult = null
            currentPage = 1
            checkByScroll = false
            reservedItems.clear()
        }
    }

    if (presenter.cameraGranted && !presenter.showRaw && !presenter.scanByScanner && !presenter.showExternal) Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if ((presenter.isAutoScan.value || presenter.showCamera.value) && presenter.errorMessage.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        CameraBox(
                            ctx = presenter.ctx!!,
                            scope = presenter.scope!!,
                            onCodeDetected = { code -> presenter.onCodeDetected(code) },
                            onStartAnalyze = { img, fm -> presenter.onStartAnalyze(img, fm) },
                            caption = {
                                if (presenter.barcode.value.isNotEmpty() && presenter.barcode.value.isNotBlank()) Box(
                                    modifier = Modifier
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White)
                                        .padding(5.dp)
                                        .align(Alignment.BottomStart),
                                ) { Text(text = "${presenter.barcode.value} x ${presenter.scanCount.intValue}") }
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bestBg()
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Button(
                            onClick = { presenter.navigator!!.navigate("codes") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                Color(0xFF2C5F88)
                            )
                        ) { Text(text = "Check list") }
                    }
                }
            } else {
                if (presenter.errorMessage.isEmpty()) Column(
                    Modifier
                        .fillMaxSize()
                        .bestBg()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(
                                Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (presenter.captureImage.value != null) {
                                    Box(
                                        modifier = Modifier
                                            .customShadow(
                                                alpha = .8f,
                                                blur = 3f,
                                                corner = 20.dp.value
                                            )
                                            .clip(RoundedCornerShape(10.dp))
                                            .width(160.dp)
                                            .height(120.dp)
                                    ) {
                                        Image(
                                            bitmap = presenter.captureImage.value!!.asImageBitmap(),
                                            contentDescription = "capture image",
                                            modifier = Modifier
                                                .clipToBounds()
                                                .scale(2f)
                                                .rotate(90f)
                                        )
                                    }
                                }
                                Text(text = presenter.barcode.value)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(text = "Quantity")
                                BasicTextField(
                                    modifier = Modifier
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFC5C5C5),
                                            shape = RoundedCornerShape(3.dp)
                                        )
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    value = presenter.manualQty.value?.toString() ?: "",
                                    onValueChange = { presenter.updateManualQty(it) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    keyboardActions = KeyboardActions(onDone = { presenter.scanMore() })
                                )
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { presenter.discard() },
                            enabled = !presenter.isKeepingCode,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(Color.Gray)
                        ) { Text(text = "Discard") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { presenter.scanMore() },
                            enabled = !presenter.isKeepingCode,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(Color(0xFF678F39))
                        ) { Text(text = "Save") }
                    }
                }
                else {
                    Dialog(onDismissRequest = {
                        presenter.onContinue()
                    }) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(5.dp))
                                    .bestBg()
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                if (presenter.captureImage.value != null) {
                                    Box(
                                        modifier = Modifier
                                            .customShadow(
                                                alpha = .8f,
                                                blur = 3f,
                                                corner = 20.dp.value
                                            )
                                            .clip(RoundedCornerShape(10.dp))
                                            .width(160.dp)
                                            .height(120.dp)
                                    ) {
                                        Image(
                                            bitmap = presenter.captureImage.value!!.asImageBitmap(),
                                            contentDescription = "capture image",
                                            modifier = Modifier
                                                .clipToBounds()
                                                .scale(2f)
                                                .rotate(90f)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "not-found",
                                        modifier = Modifier.size(40.dp),
                                        tint = Color(0xFFFF9800)
                                    )
                                }
                                Text(text = presenter.barcode.value)
                                Text(
                                    text = presenter.errorMessage,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = { presenter.onContinue() },
                                    colors = ButtonDefaults.buttonColors(
                                        Color(0xFF23547A)
                                    )
                                ) { Text(text = "Continue") }
                            }
                        }
                    }
                }
            }
        }
    } else if (presenter.scanByScanner && !presenter.showExternal) {
        //----------------------------------------------------this
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val focusRequest = FocusRequester()
                    val autoHide = remember {
                        mutableStateOf(true)
                    }
                    LaunchedEffect(density.density) {
                        snapshotFlow {
                            ime.getBottom(density) - navbar.getBottom(density)
                        }.collect {
                            val h = (it / density.density).dp
                            kHeight.value = maxOf(h, kHeight.value)
                            isKeyboardOpened.value = h == kHeight.value
                        }
                    }
                    LaunchedEffect(Unit) {
                        focusRequest.requestFocus()
                        focusRequest.captureFocus()
                    }
                    LaunchedEffect(isKeyboardOpened.value) {
                        if (isKeyboardOpened.value && autoHide.value) {
                            keyboardCtrl?.hide()
                            autoHide.value = false
                        }
                    }
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
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
                        BasicTextField(
                            value = externalCode.value,
                            singleLine = true,
                            onValueChange = { externalCode.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequest),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardCtrl?.hide()
//                                if (presenter.offline && !presenter.checkItemOffline(externalCode.value)) {
//                                    presenter.startNotFound()
//                                }
//                                currentPage = 1
                                if (presenter.offline) {
                                    onSearchOffline(externalCode.value)
                                } else {
                                    onSearch(externalCode.value)
//                                    presenter.startShowExternal()
//                                    if (presenter.isAutoScan.value) presenter.onExternalScanned(
//                                        externalCode.value,
//                                        onAdded = {
//                                            externalAdded.value = true
//                                        },
//                                        afterDelay = {
//                                            externalAdded.value = false
//                                            externalCode.value = ""
//                                        })
//                                    else presenter.startShowExternal()
                                }
                            })
                        )
                    }
                    if (presenter.isAutoScan.value) {
                        if (externalCode.value.isEmpty()) Text(text = "Waiting for scan result")
                        else if (selectedNumber.isNotEmpty()) {
                            Text(text = selectedNumber)
                            if (externalAdded.value) Text(text = "This barcode is added to temporary list")
                            else Text(text = "Adding to temporary list...")
                        }
                    } else Text(text = "Waiting for scan result")
                    if (showDialog) {
                        Dialog(
                            onDismissRequest = {
                                if (!isChecking) {
                                    showDialog = false
                                }
                            },
                            DialogProperties()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(5.dp))
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFFFFF))
                                    .padding(horizontal = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(5.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Founded Items",
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    LazyColumn(
                                        modifier = Modifier
                                            .weight(1f, fill = false),
                                        state = listState,
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        items(
                                            reservedItems.size,
                                            key = { k -> reservedItems[k].number }) { i ->
                                            Row(
                                                modifier = Modifier
                                                    .clickable {
                                                        if (!reservedItems[i].useKit) {
                                                            showDialog = false
                                                            if (presenter.isAutoScan.value) presenter.onExternalScanned(
                                                                reservedItems[i].number,
                                                                onAdded = {
                                                                    externalAdded.value = true
                                                                },
                                                                afterDelay = {
                                                                    externalAdded.value = false
                                                                    externalCode.value = ""
                                                                })
                                                            else {
                                                                selectedNumber =
                                                                    reservedItems[i].number
                                                                presenter.startShowExternal()
                                                            }
                                                        }
                                                    }
                                                    .border(
                                                        width = 1.dp,
                                                        color = Color.Gray,
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(5.dp)
                                                    .fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                val tail =
                                                    if (reservedItems[i].useKit) " (use kit)" else ""
                                                if (reservedItems[i].useKit) {
                                                    Text(
                                                        text = "${reservedItems[i].number}: ${reservedItems[i].name}${tail}",
                                                        lineHeight = 15.sp,
                                                        color = Color(0xFFDE4040)
                                                    )
                                                } else {
                                                    Text(
                                                        text = "${reservedItems[i].number}: ${reservedItems[i].name}${tail}",
                                                        lineHeight = 15.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (isChecking && checkByScroll) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(20.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularLoading(Modifier.size(20.dp))
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Button(
                                            enabled = !isChecking,
                                            onClick = {
                                                showDialog = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                Color(
                                                    0xFF25608F
                                                )
                                            ),
                                        ) { Text(text = "Close") }
                                    }
                                }
                            }
                        }
                    }
                    if (presenter.checkingOffline || isChecking) {
                        CircularLoading(Modifier.size(30.dp))
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bestBg()
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Button(
                    onClick = { presenter.navigator!!.navigate("codes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    enabled = !presenter.checkingOffline,
                    colors = ButtonDefaults.buttonColors(
                        Color(0xFF2C5F88)
                    )
                ) { Text(text = "Check list") }
            }
        }
        if (presenter.errorMessage.isNotEmpty()) Dialog(onDismissRequest = { presenter.stopNotFoundAndStopUseKit() }) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .bestBg()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "not-found",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFFFF9800)
                    )
                    Text(text = externalCode.value)
                    Text(
                        text = presenter.errorMessage,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { presenter.stopNotFoundAndStopUseKit() },
                        colors = ButtonDefaults.buttonColors(
                            Color(0xFF23547A)
                        )
                    ) { Text(text = "Continue") }
                }
            }
        }
    } else Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.camera_denied_black),
                contentDescription = "permission denied",
                modifier = Modifier
                    .scale(1.2f)
                    .alpha(.5f)
            )
            Text(text = "Camera permission is denied!", color = Color.Gray)
        }
    }
    if (presenter.showExternal) {
        Column(
            Modifier
                .fillMaxSize()
                .bestBg()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) { Text(text = selectedNumber) }
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(text = "Quantity")
                        BasicTextField(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFC5C5C5),
                                    shape = RoundedCornerShape(3.dp)
                                )
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            value = externalCount.value,
                            onValueChange = { externalCount.value = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardCtrl?.hide()
                                presenter.onExternalScanned(
                                    selectedNumber,
                                    externalCount.value.toIntOrNull() ?: 1,
                                    onAdded = {
                                        externalCode.value = ""
                                        presenter.hideExternal()
                                    })
                            })
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        externalCode.value = ""
                        presenter.hideExternal()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(Color.Gray)
                ) { Text(text = "Discard") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        keyboardCtrl?.hide()
                        presenter.onExternalScanned(
                            selectedNumber,
                            externalCount.value.toIntOrNull() ?: 1,
                            onAdded = {
                                externalCode.value = ""
                                presenter.hideExternal()
                            })
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(Color(0xFF678F39))
                ) { Text(text = "Save") }
            }
        }
    }
}