package com.biztools.stockcount.ui.pages

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.biztools.stockcount.R
import com.biztools.stockcount.presentations.pagePresentations.PriceCheckPresenter
import com.biztools.stockcount.ui.components.CircularLoading
import com.biztools.stockcount.ui.components.ItemNotFoundAlertBox
import com.biztools.stockcount.ui.extensions.bestBg

@OptIn(ExperimentalComposeUiApi::class)
@androidx.camera.core.ExperimentalGetImage
@Composable
fun PriceChecker(presenter: PriceCheckPresenter) {
    val warehouseExpanded = remember { mutableStateOf(false) }
    val isCheckedAvgCost = remember { mutableStateOf(false) }
    val isCheckedQty = remember { mutableStateOf(false) }
    val showImage = remember { mutableStateOf(false) }
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
    BackHandler(presenter.showCamera) {
        presenter.closeCamera()
    }
    BackHandler(presenter.notFound) {
        presenter.closeError()
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
    if (presenter.showCamera) {
        if (presenter.cameraGranted) Box(modifier = Modifier.fillMaxSize()) {
            CameraBox(
                ctx = presenter.ctx!!,
                scope = presenter.scope!!,
                onCodeDetected = presenter.onCodeDetected,
                onStartAnalyze = { img, f -> presenter.onStartAnalyze(img, f) }
            )
        }
        else {
            presenter.requestPermission()
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column {
                    Text(text = "Need camera permission")
                    Button(onClick = { presenter.closeCamera() }) {
                        Text(text = "Cancel")
                    }
                }
            }
        }
    } else Column(
        modifier = Modifier
            .fillMaxSize()
            .bestBg()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(text = "Warehouse")
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFBB9D9D),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .fillMaxWidth()
                                .clickable(enabled = !presenter.checking) {
                                    warehouseExpanded.value = !warehouseExpanded.value
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = presenter.selectedWarehouse ?: "Select warehouse")
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "select warehouse"
                            )
                        }
                        DropdownMenu(
                            expanded = warehouseExpanded.value,
                            modifier = Modifier.width((LocalConfiguration.current.screenWidthDp - 16).dp),
                            onDismissRequest = { warehouseExpanded.value = false }) {
                            repeat(presenter.warehouse.warehouses.size) {
                                DropdownMenuItem(
                                    text = { Text(text = presenter.warehouse.warehouses[it].name) },
                                    enabled = !presenter.checking,
                                    onClick = {
                                        presenter.selectWarehouse(presenter.warehouse.warehouses[it].name)
                                        warehouseExpanded.value = false
                                    })
                            }
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(visible = !presenter.selectedWarehouse.isNullOrEmpty() && !presenter.selectedWarehouse.isNullOrBlank()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            LaunchedEffect(Unit) {
                                focusRequest.requestFocus()
                                focusRequest.captureFocus()
                            }
                            Text(text = "Item Number/Barcode")
                            BasicTextField(
                                value = presenter.barcode,
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFBB9D9D),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .focusRequester(focusRequest)
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                singleLine = true,
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardCtl?.hide()
                                    presenter.onCheckPrice()
                                }),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                onValueChange = { presenter.updateBarcode(it) },
                                enabled = !presenter.selectedWarehouse.isNullOrEmpty() && !presenter.selectedWarehouse.isNullOrBlank() && !presenter.checking
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(text = "Item Number")
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFBB9D9D),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) { Text(text = presenter.currentItem?.number ?: "") }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Sale Price")
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFBB9D9D),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (presenter.currentItem == null) "0.00"
                                        else presenter.currentItem!!.price.toString()
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .padding(end = 20.dp)
                                    .width(68.dp)
                                    .height(51.dp)
                            ) {
                                if (presenter.captureImage != null) Image(
                                    bitmap = presenter.captureImage!!.asImageBitmap(),
                                    contentDescription = "capture image",
                                    modifier = Modifier
                                        .clipToBounds()
                                        .clickable { showImage.value = true }
                                        .scale(2f)
                                        .rotate(90f)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(text = "Item Name")
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFBB9D9D),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) { Text(text = presenter.currentItem?.name ?: "") }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            if (!isCheckedQty.value) Button(
                                onClick = { isCheckedQty.value = true },
                                enabled = presenter.currentItem != null && !presenter.checking,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(5.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF144B8F)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "search",
                                        modifier = Modifier.width(20.dp)
                                    )
                                    Text(text = "Check Quantity")
                                }
                            }
                            AnimatedVisibility(visible = isCheckedQty.value) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFBB9D9D),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Quantity")
                                        Text(
                                            text = if (presenter.currentItem == null) "0"
                                            else presenter.currentItem!!.qty.toString()
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Total Quantity")
                                        Text(
                                            text = if (presenter.currentItem == null) "0"
                                            else presenter.currentItem!!.total.toString()
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "PO Quantity")
                                        Text(
                                            text = if (presenter.currentItem == null) "0"
                                            else presenter.currentItem!!.poQty.toString()
                                        )
                                    }
                                }
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            if (!isCheckedAvgCost.value) Button(
                                onClick = { isCheckedAvgCost.value = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = presenter.currentItem != null && !presenter.checking,
                                shape = RoundedCornerShape(5.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF144B8F)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "search",
                                        modifier = Modifier.width(20.dp)
                                    )
                                    Text(text = "Check Average Cost")
                                }
                            }
                            AnimatedVisibility(visible = isCheckedAvgCost.value) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFBB9D9D),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = "Average Cost")
                                        Text(
                                            text = if (presenter.currentItem == null) "0.00"
                                            else presenter.currentItem!!.avgCost.toString()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Button(
            enabled = !presenter.selectedWarehouse.isNullOrEmpty() && !presenter.selectedWarehouse.isNullOrBlank(),
            onClick = { presenter.onQrClick() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF52721F)
            )
        ) {
            if (presenter.checking) CircularLoading(modifier = Modifier.size(20.dp))
            else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.scan),
                        contentDescription = "search",
                        modifier = Modifier.width(20.dp)
                    )
                    Text(text = "Scan")
                }
            }
        }
    }
    if (showImage.value) {
        Dialog(
            onDismissRequest = { showImage.value = false },
            DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(
                    bitmap = presenter.captureImage!!.asImageBitmap(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .rotate(90f)
                        .scale(2f),
                    contentDescription = "view image"
                )
            }
        }
    }
    if (presenter.notFound) ItemNotFoundAlertBox(
        onClose = { presenter.closeError() }) {
        presenter.gotoAddItem()
    }
}