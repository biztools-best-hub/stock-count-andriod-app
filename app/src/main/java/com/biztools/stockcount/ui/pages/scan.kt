package com.biztools.stockcount.ui.pages

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.biztools.stockcount.R
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
//    val showExternal = remember {
//        mutableStateOf(false)
//    }

    if (presenter.cameraGranted && !presenter.showRaw && !presenter.scanByScanner && !presenter.showExternal) Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if ((presenter.isAutoScan.value || presenter.showCamera.value) && !presenter.notFound) {
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
                if (!presenter.notFound) Column(
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
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(Color.Gray)
                        ) { Text(text = "Discard") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { presenter.scanMore() },
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
                                    text = "Couldn't find item with this barcode",
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
                                if (presenter.offline && !presenter.checkItemOffline(externalCode.value)) {
                                    presenter.startNotFound()
                                } else {
//                                    presenter.startShowExternal()
                                    if (presenter.isAutoScan.value) presenter.onExternalScanned(
                                        externalCode.value,
                                        onAdded = {
                                            externalAdded.value = true
                                        },
                                        afterDelay = {
                                            externalAdded.value = false
                                            externalCode.value = ""
                                        })
                                    else presenter.startShowExternal()
                                }
                            })
                        )
                    }
                    if (presenter.isAutoScan.value) {
                        if (externalCode.value.isEmpty()) Text(text = "Waiting for scan result")
                        else {
                            Text(text = externalCode.value)
                            if (externalAdded.value) Text(text = "This barcode is added to temporary list")
                            else Text(text = "Adding to temporary list...")
                        }
                    } else Text(text = "Waiting for scan result")
                    if (presenter.checkingOffline) {
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
        if (presenter.notFound) Dialog(onDismissRequest = { presenter.stopNotFound() }) {
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
                        text = "Couldn't find item with this barcode",
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { presenter.stopNotFound() },
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
                painter = painterResource(id = if (presenter.isDarkTheme) R.drawable.camera_denied_white else R.drawable.camera_denied_black),
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
                    ) { Text(text = externalCode.value) }
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
                                    externalCode.value,
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
                            externalCode.value,
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