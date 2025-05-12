package com.biztools.stockcount.ui.pages

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.biztools.stockcount.api.RestAPI
import com.biztools.stockcount.api.StockApi
import com.biztools.stockcount.models.ItemOnHandResult
import com.biztools.stockcount.models.OnHandItem
import com.biztools.stockcount.ui.components.CircularLoading
import com.biztools.stockcount.ui.extensions.bestBg
import com.biztools.stockcount.ui.extensions.customShadow
import com.biztools.stockcount.ui.extensions.innerShadow
import com.biztools.stockcount.ui.utilities.toLocalString
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalComposeUiApi::class)
@Composable
fun OnHand(
    ctx: Context,
    navigator: NavHostController,
    drawer: DrawerState,
    scope: CoroutineScope,
) {
    val cameraPermission = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val listState = rememberLazyListState()
    val reservedItems = remember {
        mutableListOf<OnHandItem>()
    }
    var checkByScroll by remember {
        mutableStateOf(false)
    }
    var prevPage by remember {
        mutableIntStateOf(1)
    }
    var showDialog by remember {
        mutableStateOf(false)
    }
    var currentPage by remember {
        mutableIntStateOf(1)
    }
    var onHandResult by remember {
        mutableStateOf<ItemOnHandResult?>(null)
    }
    var barcode by remember {
        mutableStateOf("")
    }
    var isCameraOpened by remember {
        mutableStateOf(false)
    }
    var isChecking by remember {
        mutableStateOf(false)
    }
    var init by remember {
        mutableStateOf(false)
    }
    var firstFocus by remember {
        mutableStateOf(true)
    }
    val navbar = WindowInsets.navigationBars
    val ime = WindowInsets.ime
    val density = LocalDensity.current
    var kHeight by remember {
        mutableStateOf(0.dp)
    }
    val keyboardCtrl = LocalSoftwareKeyboardController.current
    var isKeyboardOpened by remember {
        mutableStateOf(false)
    }
    val focusRequest = FocusRequester()
    fun onSuccessCall(res: ItemOnHandResult) {
        onHandResult = res
        isChecking = false
        if (res.items.isNotEmpty()) {
            reservedItems.addAll(res.items.filter { reservedItems.all { r -> r.number != it.number } })
            prevPage = currentPage
            if (!checkByScroll) showDialog = true
        } else if (!checkByScroll) showDialog = false
    }

    fun onSearch() {
        if (barcode.isEmpty() || barcode.isBlank()) return
        isChecking = true
        if (onHandResult != null) {
            val tempResult = onHandResult
            tempResult!!.number = ""
            onHandResult = tempResult
        }
        if (currentPage < 2) reservedItems.clear()
        try {
            val searchApi = RestAPI.create<StockApi>()
            val searchCall = searchApi.searchItems(barcode, currentPage, 20)
            RestAPI.execute(
                searchCall,
                scope,
                onSuccess = { res -> onSuccessCall(res) },
                onError = { e ->
                    Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
                    isChecking = false
                })
        } catch (e: Exception) {
            Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
            isChecking = false
        }
    }

    fun onCheck(number: String) {
        if (number.isEmpty() || number.isBlank()) return
        isChecking = true
        reservedItems.clear()
        try {
            val onHandApi = RestAPI.create<StockApi>()
            val onHandCall = onHandApi.checkOnHand(number)
            RestAPI.execute(
                onHandCall,
                scope,
                onSuccess = { res -> onSuccessCall(res) },
                onError = { e ->
                    Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
                    onHandResult = null
                    isChecking = false
                })
        } catch (e: Exception) {
            Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
            onHandResult = null
            isChecking = false
        }
    }

    val reachedBottom: Boolean by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= 10 && lastVisibleItem.index + 1 > listState.layoutInfo.totalItemsCount - 1
        }
    }
    LaunchedEffect(Unit) {
        navigator.currentBackStackEntry?.arguments?.getString("number")?.let { num ->
            if (num != "..") {
                barcode = num
                onCheck(num)
            }
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
            onSearch()
        }
    }
    BackHandler(!showDialog) {
        if (drawer.isOpen) scope.launch { drawer.close() }
        else navigator.navigate("menu")
    }
    BackHandler(isCameraOpened) {
        if (isCameraOpened) isCameraOpened = false
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
    LaunchedEffect(cameraPermission.permissionRequested, cameraPermission.hasPermission) {
        if (cameraPermission.permissionRequested) init = true
        if (!init) {
            if (cameraPermission.hasPermission) init = true
            else cameraPermission.launchPermissionRequest()
        }
    }
    LaunchedEffect(density.density) {
        snapshotFlow {
            ime.getBottom(density) - navbar.getBottom(density)
        }.collect {
            val h = (it / density.density).dp
            kHeight = maxOf(h, kHeight)
            isKeyboardOpened = h >= kHeight
        }
    }

    LaunchedEffect(isKeyboardOpened) {
        if (isKeyboardOpened && firstFocus) {
            keyboardCtrl?.hide()
            firstFocus = false
        }
    }
    if (!init) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularLoading()
        }
    } else if (cameraPermission.hasPermission) {
        if (isCameraOpened) {
            CameraBox(
                ctx,
                scope,
                onCodeDetected = { code ->
                    barcode = code
                    onSearch()
                    isCameraOpened = false
                },
                onStartAnalyze = { _, _ -> })
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .bestBg(),
            ) {
                Column(
                    Modifier
                        .padding(start = 5.dp, end = 5.dp, top = 5.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = Color(0xFF949393),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LaunchedEffect(true) {
                            focusRequest.requestFocus()
                            focusRequest.captureFocus()
                        }
                        BasicTextField(
                            enabled = !isChecking,
                            singleLine = true,
                            value = barcode,
                            onValueChange = { barcode = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequest),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardCtrl?.hide()
                                onSearch()
                            })
                        )
                        ElevatedButton(
                            contentPadding = PaddingValues(all = 0.dp),
                            shape = RoundedCornerShape(2.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3893A0),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .height(20.dp)
                                .padding(0.dp),
                            onClick = {
                                keyboardCtrl?.hide()
                                onSearch()
                            }) {
                            Text("SEARCH", fontSize = 10.sp)
                        }
                    }
                }
                if (isChecking && !showDialog) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularLoading()
                    }
                } else {
                    AnimatedVisibility(visible = onHandResult != null && onHandResult!!.items.isEmpty() && onHandResult!!.number.isNotEmpty()) {
                        Column(
                            Modifier
                                .padding(start = 5.dp, end = 5.dp)
                                .fillMaxWidth(),
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "Item Number",
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                    Text(text = onHandResult!!.number, lineHeight = 15.sp)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "PDTCat",
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = onHandResult!!.pdtCat.ifEmpty { "--" },
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                            Row(Modifier.fillMaxWidth()) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "Item Name",
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                    Text(text = onHandResult!!.name, lineHeight = 15.sp)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "SubCat 1",
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = onHandResult!!.subCat1.ifEmpty { "--" },
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                            Row(Modifier.fillMaxWidth()) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "Group",
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = onHandResult!!.group.ifEmpty { "--" },
                                        lineHeight = 15.sp
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "SubCat 2",
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = onHandResult!!.subCat2.ifEmpty { "--" },
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "Brand",
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = onHandResult!!.brand.ifEmpty { "--" },
                                        lineHeight = 15.sp
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "SubCat 3",
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = onHandResult!!.subCat3.ifEmpty { "--" },
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                            Row(Modifier.fillMaxWidth()) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "Retail Price",
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = onHandResult!!.price.toLocalString,
                                        lineHeight = 15.sp
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "Total SOH",
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = onHandResult!!.totalSOH.toLocalString,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                            Row(Modifier.fillMaxWidth()) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "Whole Sale Price",
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = onHandResult!!.wholeSalePrice.toLocalString,
                                        lineHeight = 15.sp
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "Unit Cost",
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = onHandResult!!.unitCost.toLocalString,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 5.dp)
                            ) {
                                Text(
                                    text = "Description",
                                    style = TextStyle(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = onHandResult!!.description.ifEmpty { "--" },
                                    lineHeight = 15.sp
                                )
                                if (onHandResult!!.description2.isNotEmpty() && onHandResult!!.description2 != onHandResult!!.description) {
                                    Text(text = onHandResult!!.description2, lineHeight = 15.sp)
                                }
                            }
                        }
                    }
                    if (onHandResult != null && onHandResult!!.items.isEmpty() && onHandResult!!.number.isNotEmpty()) {
                        Column(
                            Modifier
                                .weight(1f)
                                .background(Color(0xFFDFDFDF))
                                .innerShadow(
                                    blur = 5.dp,
                                    color = Color(0xC87E7E7E)
                                )
                                .padding(5.dp), verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Row(
                                Modifier
                                    .customShadow(x = 1f, y = 1f, alpha = 0.1f)
                                    .fillMaxWidth()
                                    .background(Color(0xFF4B4B4B))
                                    .padding(5.dp)
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f), text = "Location",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFFFFF)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(
                                        text = "On hand",
                                        modifier = Modifier.defaultMinSize(minWidth = 58.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFFFFF),
                                        textAlign = TextAlign.Right
                                    )
                                    Text(
                                        text = "Available",
                                        modifier = Modifier.defaultMinSize(minWidth = 58.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFFFFF),
                                        textAlign = TextAlign.Right
                                    )
                                    Text(
                                        text = "On hold",
                                        modifier = Modifier.defaultMinSize(minWidth = 58.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFFFFF),
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize(),
                                reverseLayout = true,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(
                                    count = onHandResult!!.warehouses.size,
                                    key = { idx -> onHandResult!!.warehouses[idx].location }) { i ->
                                    val wh = onHandResult!!.warehouses[i]
                                    Column(
                                        Modifier
                                            .customShadow(x = 1f, y = 1f, alpha = 0.1f)
                                            .fillMaxWidth()
                                            .background(Color.White)
                                            .padding(5.dp),
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                modifier = Modifier.weight(1f),
                                                text = if (wh.description.isEmpty()) wh.location else "${wh.location}:${wh.description}",
                                                lineHeight = 15.sp
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                                Text(
                                                    text = wh.qty.toLocalString,
                                                    modifier = Modifier.defaultMinSize(minWidth = 58.dp),
                                                    textAlign = TextAlign.Right, lineHeight = 15.sp
                                                )
                                                Text(
                                                    text = wh.available.toLocalString,
                                                    modifier = Modifier.defaultMinSize(minWidth = 58.dp),
                                                    textAlign = TextAlign.Right, lineHeight = 15.sp
                                                )
                                                Text(
                                                    text = wh.onHold.toLocalString,
                                                    modifier = Modifier.defaultMinSize(minWidth = 58.dp),
                                                    textAlign = TextAlign.Right, lineHeight = 15.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                                    .padding(5.dp),
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
                                        Text(text = "Founded Items", textAlign = TextAlign.Center)
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
                                                        showDialog = false
                                                        onCheck(reservedItems[i].number)
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
                                                Text(
                                                    text = "${reservedItems[i].number}: ${reservedItems[i].name}",
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }
                                    }
                                    if (isChecking && checkByScroll) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(20.dp), contentAlignment = Alignment.Center
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
                                            colors = ButtonDefaults.buttonColors(Color(0xFF25608F)),
                                        ) { Text(text = "Close") }
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp)
                        .fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = {
                            isCameraOpened = true
                        }, modifier = Modifier
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(Color(0xFF25608F)),
                        enabled = !isChecking
                    ) { Text(text = "Scan") }
                    Button(
                        onClick = {
                            barcode = ""
                            if (onHandResult != null) {
                                onHandResult!!.number = ""
                            }
                        }, modifier = Modifier
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(Color(0xFF25608F)),
                        enabled = !isChecking
                    ) { Text(text = "Clear") }
                    Button(
                        onClick = {
                            if (onHandResult != null && onHandResult!!.number.isNotEmpty()) {
                                navigator.navigate("check-promo/${onHandResult!!.number}")
                            }
                        }, modifier = Modifier
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(Color(0xFF25608F)),
                        enabled = !isChecking && onHandResult != null && onHandResult!!.number.isNotEmpty()
                    ) { Text(text = "Promotion") }
                }
            }
        }
    } else Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "alert",
                modifier = Modifier.size(40.dp),
                tint = Color.Red
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Camera permission not granted")
        }
    }
}