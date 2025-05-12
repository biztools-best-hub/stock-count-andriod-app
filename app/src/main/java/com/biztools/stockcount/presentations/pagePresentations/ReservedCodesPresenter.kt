package com.biztools.stockcount.presentations.pagePresentations

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.biztools.stockcount.api.RestAPI
import com.biztools.stockcount.api.StockApi
import com.biztools.stockcount.models.DuplicateItem
import com.biztools.stockcount.models.DuplicateItemInput
import com.biztools.stockcount.models.ReservedBarcodeData
import com.biztools.stockcount.models.StockCountBatchInput
import com.biztools.stockcount.models.StockCountInput
import com.biztools.stockcount.presentations.layoutPresentations.BasePresenter
import com.biztools.stockcount.stores.BarcodesStore
import com.biztools.stockcount.stores.SettingStore
import com.biztools.stockcount.ui.components.CircularLoading
import com.biztools.stockcount.ui.extensions.bestBg
import com.biztools.stockcount.ui.extensions.borderBottom
import com.biztools.stockcount.ui.extensions.borderTop
import com.biztools.stockcount.ui.pages.ItemGroup
import com.biztools.stockcount.ui.pages.ReservedCodes
import com.biztools.stockcount.ui.pages.TempItem
import com.biztools.stockcount.ui.utilities.NoRippleInteraction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class DuplicateItemGroup(val warehouse: String, val items: List<DuplicateItem>)

class ReservedCodesPresenter(
    ctx: Context? = null,
    scope: CoroutineScope? = null,
    setting: SettingStore? = null,
    navigator: NavHostController? = null,
    page: MutableState<String>? = null,
    drawer: DrawerState? = null,
    private val device: String = ""
) : BasePresenter(ctx, scope, setting, navigator, page, drawer) {
    private var _store: BarcodesStore? = null
    private var _barcodes: State<List<ReservedBarcodeData>?>? = null
    val barcodes get() = _barcodes
    private var _isFetching: MutableState<Boolean> = mutableStateOf(false)
    private var _duplicateItems: MutableList<DuplicateItem> = mutableListOf()
    private var _currentItem: MutableState<DuplicateItemInput?> = mutableStateOf(null)
    private var _needDecision: MutableState<Boolean> = mutableStateOf(false)
    private var _duplicateInputs: MutableList<DuplicateItemInput> = mutableListOf()
    private var _needWarehouse = mutableStateOf(false)
    private var _notFoundItems = mutableStateListOf<String>()
    val needDecision get() = _needDecision.value
    val needWarehouse get() = _needWarehouse.value
    val isFetching get() = _isFetching.value
    val notFoundItems get() = _notFoundItems
    override val render: @Composable (content: (() -> Unit)?) -> Unit = {
        _store = BarcodesStore(ctx!!)
        _barcodes = _store!!.codes.collectAsState(initial = null)
        _isFetching = remember { mutableStateOf(false) }
        _needWarehouse = remember { mutableStateOf(false) }
        _duplicateItems = remember { mutableListOf() }
        _duplicateInputs = remember { mutableListOf() }
        _notFoundItems = remember { mutableStateListOf() }
        if (_duplicateItems.size > 0 && _currentItem.value == null) {
            _currentItem = remember {
                mutableStateOf(
                    DuplicateItemInput(
                        productNumber = _duplicateItems[0].productNumber,
                        decision = "m",
                        warehouse = _duplicateItems[0].warehouse
                    )
                )
            }
        }
        _needDecision = remember { mutableStateOf(false) }
        val initializing = remember(_barcodes!!.value) {
            mutableStateOf(_barcodes!!.value == null)
        }
        if (initializing.value) CircularLoading()
        else super.render { ReservedCodes(this) }
    }
    val content: @Composable () -> Unit = {}
    val showDecision: @Composable () -> Unit = {
        Dialog(
            onDismissRequest = { _needDecision.value = false },
            DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isDarkTheme) Color(0xFF1F1E1E)
                        else Color(0xFFFFFFFF)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .borderBottom(
                            color = if (isDarkTheme) Color(0xFF333333)
                            else Color(0xFFBEBEBE)
                        )
                        .fillMaxWidth()
                        .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Some items are duplicated in stock",
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Need your decision",
                        textAlign = TextAlign.Center
                    )
                }
                if (_duplicateItems.size > 0) {
                    val warehouses = _duplicateItems.groupBy { d -> d.warehouse }
                        .filter { d -> d.value.isNotEmpty() }.map { d ->
                            DuplicateItemGroup(
                                warehouse = d.value[0].warehouse,
                                items = d.value
                            )
                        }
                    LazyColumn(modifier = Modifier
                        .background(
                            if (isDarkTheme) Color(0xFF000000)
                            else Color(0xFFD8D8D8)
                        )
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        content = {
                            items(
                                count = warehouses.size,
                                key = { k -> warehouses[k].warehouse }
                            ) { i ->
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(
                                            if (isDarkTheme) Color(0xFF333232)
                                            else Color(0xFFFFFFFF)
                                        )
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = warehouses[i].warehouse,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                    repeat(warehouses[i].items.size) { j ->
                                        val modifier = if (j < 0) Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 10.dp)
                                        else if (j >= warehouses[i].items.size - 1) Modifier
                                            .borderTop(
                                                color = if (isDarkTheme) Color(0xFF727272)
                                                else Color(0xFFD8D8D8)
                                            )
                                            .fillMaxWidth()
                                            .padding(top = 10.dp)
                                        else Modifier
                                            .borderTop(
                                                color = if (isDarkTheme) Color(0xFF727272)
                                                else Color(0xFFD8D8D8)
                                            )
                                            .fillMaxWidth()
                                            .padding(top = 10.dp, bottom = 10.dp)
                                        val off = remember { Animatable(0f) }
                                        val mergeColor = remember {
                                            mutableStateOf(Color(0xFFE7E7E7))
                                        }
                                        val replaceColor = remember {
                                            mutableStateOf(Color(0xFF272727))
                                        }
                                        val ignoreColor = remember {
                                            mutableStateOf(Color(0xFF272727))
                                        }
                                        LaunchedEffect(key1 = _currentItem.value) {
                                            if (_currentItem.value != null && _currentItem.value!!.productNumber == warehouses[i].items[j].productNumber) {
                                                scope!!.launch {
                                                    off.animateTo(
                                                        targetValue = when (_currentItem.value!!.decision) {
                                                            "m" -> 0f
                                                            "r" -> 60.dp.value
                                                            else -> 120.dp.value
                                                        },
                                                        animationSpec = tween(
                                                            durationMillis = 100,
                                                            easing = LinearEasing
                                                        )
                                                    )
                                                }
                                                val idx = _duplicateInputs.indexOfFirst { d ->
                                                    d.productNumber == _currentItem.value!!.productNumber &&
                                                            d.warehouse == _currentItem.value!!.warehouse
                                                }
                                                _duplicateInputs[idx].decision =
                                                    _currentItem.value!!.decision
                                                mergeColor.value =
                                                    if (_currentItem.value!!.decision == "m")
                                                        Color(0xFFE7E7E7)
                                                    else (if (isDarkTheme) Color(0xFF9C9B9B)
                                                    else Color(0xFF272727))
                                                replaceColor.value =
                                                    if (_currentItem.value!!.decision == "r")
                                                        Color(0xFFE7E7E7)
                                                    else (if (isDarkTheme) Color(0xFF9C9B9B)
                                                    else Color(0xFF272727))
                                                ignoreColor.value =
                                                    if (_currentItem.value!!.decision == "i")
                                                        Color(0xFFE7E7E7)
                                                    else (if (isDarkTheme) Color(0xFF9C9B9B)
                                                    else Color(0xFF272727))
                                            }
                                        }
                                        Column(modifier) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = warehouses[i].items[j].productNumber)
                                                Box(
                                                    Modifier.border(
                                                        width = 1.dp,
                                                        color = Color(0xFF037777)
                                                    )
                                                ) {
                                                    Row(modifier = Modifier.width(180.dp)) {
                                                        Box(
                                                            modifier = Modifier
                                                                .offset(x = Dp(off.value))
                                                                .width(60.dp)
                                                                .height(28.dp)
                                                                .background(Color(0xFF037777))
                                                        )
                                                    }
                                                    Row {
                                                        Box(
                                                            modifier = Modifier
                                                                .width(60.dp)
                                                                .clip(shape = RectangleShape)
                                                                .clickable(
                                                                    interactionSource = NoRippleInteraction(),
                                                                    null,
                                                                    onClick = {
                                                                        scope!!.launch {
                                                                            _currentItem.value =
                                                                                DuplicateItemInput(
                                                                                    productNumber = warehouses[i].items[j].productNumber,
                                                                                    decision = "m",
                                                                                    warehouse = warehouses[i].items[j].warehouse
                                                                                )
                                                                        }
                                                                    })
                                                                .padding(vertical = 4.dp)
                                                                .height(20.dp)
                                                                .width(44.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "Merge",
                                                                fontSize = 11.sp,
                                                                color = mergeColor.value,
                                                                textAlign = TextAlign.Center
                                                            )
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .width(60.dp)
                                                                .clip(shape = RectangleShape)
                                                                .padding(vertical = 4.dp)
                                                                .clickable(
                                                                    interactionSource = NoRippleInteraction(),
                                                                    null,
                                                                    onClick = {
                                                                        scope!!.launch {
                                                                            _currentItem.value =
                                                                                DuplicateItemInput(
                                                                                    productNumber = warehouses[i].items[j].productNumber,
                                                                                    decision = "r",
                                                                                    warehouse = warehouses[i].items[j].warehouse
                                                                                )
                                                                        }
                                                                    })
                                                                .height(20.dp)
                                                                .width(44.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "Replace",
                                                                fontSize = 11.sp,
                                                                color = replaceColor.value,
                                                                textAlign = TextAlign.Center
                                                            )
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .width(60.dp)
                                                                .clip(shape = RectangleShape)
                                                                .padding(vertical = 4.dp)
                                                                .clickable(
                                                                    interactionSource = NoRippleInteraction(),
                                                                    null,
                                                                    onClick = {
                                                                        scope!!.launch {
                                                                            _currentItem.value =
                                                                                DuplicateItemInput(
                                                                                    productNumber = warehouses[i].items[j].productNumber,
                                                                                    decision = "i",
                                                                                    warehouse = warehouses[i].items[j].warehouse
                                                                                )
                                                                        }
                                                                    })
                                                                .height(20.dp)
                                                                .width(44.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "Ignore",
                                                                fontSize = 11.sp,
                                                                color = ignoreColor.value,
                                                                textAlign = TextAlign.Center
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                val oldColor = Color(0xFFBB5101)
                                                val newColor = Color(0xFF729B4A)
                                                Text(
                                                    text = "Old count: " + warehouses[i].items[j].oldCount.toString(),
                                                    fontSize = 12.sp,
                                                    color = oldColor
                                                )
                                                Text(
                                                    text = "New count: " + warehouses[i].items[j].newCount.toString(),
                                                    fontSize = 12.sp,
                                                    color = newColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        })
                }
                Row(
                    modifier = Modifier
                        .borderTop(
                            color = if (isDarkTheme) Color(0xFF333333)
                            else Color(0xFFD8D8D8)
                        )
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ElevatedButton(
                        onClick = {
                            _needDecision.value = false
                            _currentItem.value = null
                            _duplicateItems.clear()
                            _duplicateInputs.clear()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(Color.Gray, Color.White)
                    ) { Text(text = "Cancel") }
                    ElevatedButton(
                        onClick = { saveWhenDuplicate() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(Color(0xFF5C7C36), Color.White)
                    ) { Text(text = "Save") }
                }
            }
        }
    }

    fun clearData(callBack: () -> Unit) {
        scope?.launch {
            _store?.clearCodes()
            callBack()
        }
    }

    val showNotFoundItems: @Composable () -> Unit = {
        val clipboard = LocalClipboardManager.current
        Dialog(
            onDismissRequest = { _notFoundItems.clear() }, DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .fillMaxWidth(.9f)
                    .bestBg(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(text = "Stock counting record is saved", textAlign = TextAlign.Center)
                        Text(
                            text = "But some items below are not found",
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    LazyColumn(
                        modifier = Modifier
                            .borderTop(color = Color(0xFFA8A8A8))
                            .borderBottom(color = Color(0xFFA8A8A8))
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        items(count = _notFoundItems.size, key = { k -> _notFoundItems[k] }) {
                            Text(text = _notFoundItems[it])
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboard.setText(AnnotatedString(_notFoundItems.joinToString("\n")))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(Color(0xFF24557C))
                        ) { Text(text = "Copy") }
                        Button(
                            onClick = { _notFoundItems.clear() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(Color.Gray)
                        ) { Text(text = "Close") }
                    }
                }
            }
        }
    }

    val showSpinner: @Composable () -> Unit = {
        Dialog(onDismissRequest = { _isFetching.value = false }) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularLoading()
            }
        }
    }
    val warehouseDialog: @Composable () -> Unit = {
        Dialog(
            onDismissRequest = { _needWarehouse.value = false },
            DialogProperties(dismissOnClickOutside = false)
        ) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                val wh = remember { mutableStateOf("Select warehouse") }
                val expanded = remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.fillMaxHeight(0.3f))
                    Column {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    if (isDarkTheme) Color(0xFF313131)
                                    else Color(0xFFFFFFFF)
                                )
                                .width(200.dp)
                                .clickable { expanded.value = !expanded.value }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = wh.value)
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "show-warehouse"
                            )
                        }
                        DropdownMenu(
                            expanded = expanded.value,
                            modifier = Modifier.width(200.dp),
                            onDismissRequest = { expanded.value = false }) {
                            val warehouses = _barcodes!!.value!!.groupBy { g -> g.warehouse }
                                .filter { g -> g.value.isNotEmpty() }.map { g ->
                                    ItemGroup(
                                        warehouse = g.value[0].warehouse,
                                        items = g.value.map { c ->
                                            TempItem(
                                                code = c.code,
                                                count = c.count,
                                                timeStamp = c.timestamp
                                            )
                                        })
                                }
                            repeat(warehouses.size) { n ->
                                DropdownMenuItem(
                                    text = { Text(text = warehouses[n].warehouse) },
                                    onClick = {
                                        wh.value = warehouses[n].warehouse
                                        _needWarehouse.value = false
                                        saveCodes(wh.value)
                                    })
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveWhenDuplicate() {
        try {
            _needDecision.value = false
            _isFetching.value = true
            val api = RestAPI.create<StockApi>(deviceId = device)
            val call = api.saveStockFile(
                StockCountBatchInput(
                    items = _barcodes!!.value!!.map {
                        StockCountInput(
                            itemNumber = it.code,
                            timestamp = it.timestamp.time,
                            count = it.count,
                            warehouse = it.warehouse
                        )
                    }.toMutableList(),
                    duplicateItems = _duplicateInputs
                )
            )
            RestAPI.execute(call, scope!!, onSuccess = { r ->
                if (!r.result) {
                    _isFetching.value = false
                    _duplicateItems.clear()
                    _duplicateInputs.clear()
                    _currentItem.value = null
                    Toast.makeText(ctx, "Server error!", Toast.LENGTH_LONG).show()
                    return@execute
                }
                _duplicateItems.clear()
                _duplicateInputs.clear()
                _currentItem.value = null
                clearData {
                    Toast.makeText(ctx, "Stock is saved", Toast.LENGTH_LONG).show()
                }
                if (r.notFoundItems.isNotEmpty()) _notFoundItems.addAll(r.notFoundItems)
                _isFetching.value = false
            }, onError = { e ->
                _isFetching.value = false
                Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
            })
        } catch (e: Exception) {
            _isFetching.value = false
            _duplicateItems.clear()
            _duplicateInputs.clear()
            _currentItem.value = null
            Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun saveCodes(wh: String? = null) {
        try {
            _isFetching.value = true
            val api = RestAPI.create<StockApi>(deviceId = device)
            var sendItem = _barcodes!!.value!!.map {
                StockCountInput(
                    itemNumber = it.code,
                    timestamp = it.timestamp.time,
                    count = it.count,
                    warehouse = it.warehouse
                )
            }.toMutableList()
            if (!wh.isNullOrEmpty() && wh.isNotBlank()) {
                sendItem = sendItem.filter { v -> v.warehouse == wh }.toMutableList()
            }
            val call = api.saveStockFile(
                StockCountBatchInput(
                    items = sendItem,
                    duplicateItems = mutableListOf()
                )
            )
            RestAPI.execute(call, scope!!, onSuccess = { r ->
                if (!r.result) {
                    _isFetching.value = false
                    if (r.duplicateItems.isEmpty()) {
                        Toast.makeText(ctx, "Server error!", Toast.LENGTH_LONG).show()
                        return@execute
                    }
                    _duplicateItems.addAll(r.duplicateItems)
                    _duplicateInputs.addAll(r.duplicateItems.map {
                        DuplicateItemInput(
                            productNumber = it.productNumber,
                            decision = "m",
                            warehouse = it.warehouse
                        )
                    })
                    _currentItem.value = DuplicateItemInput(
                        productNumber = _duplicateItems[0].productNumber,
                        decision = "m",
                        warehouse = _duplicateItems[0].warehouse
                    )
                    _needDecision.value = true
                } else {
                    if (wh.isNullOrEmpty() || wh.isBlank()) {
                        clearData {
                            Toast.makeText(ctx, "Stock is saved", Toast.LENGTH_LONG).show()
                        }
                    } else scope!!.launch { _store!!.removeIf { c -> c.warehouse == wh } }
                    if (r.notFoundItems.isNotEmpty()) _notFoundItems.addAll(r.notFoundItems)
                    _isFetching.value = false
                }
            }, onError = { e ->
                _isFetching.value = false
                Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
            })
        } catch (e: Exception) {
            _isFetching.value = false
            Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun removeCode(code: String, warehouse: String) {
        scope?.launch { _store?.remove(code, warehouse) }
    }
}