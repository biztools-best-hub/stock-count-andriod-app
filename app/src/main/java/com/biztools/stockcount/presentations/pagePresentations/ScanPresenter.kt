package com.biztools.stockcount.presentations.pagePresentations

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.biztools.stockcount.models.GetWarehousesResult
import com.biztools.stockcount.models.Item
import com.biztools.stockcount.models.ReservedBarcodeData
import com.biztools.stockcount.models.Warehouse
import com.biztools.stockcount.presentations.layoutPresentations.BasePresenter
import com.biztools.stockcount.stores.BarcodesStore
import com.biztools.stockcount.stores.SettingStore
import com.biztools.stockcount.ui.extensions.bestBg
import com.biztools.stockcount.ui.pages.Scan
import com.biztools.stockcount.ui.utilities.NoRippleInteraction
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@ExperimentalGetImage
class ScanPresenter(
    ctx: Context? = null,
    scope: CoroutineScope? = null,
    setting: SettingStore? = null,
    navigator: NavHostController? = null,
    page: MutableState<String>? = null,
    drawer: DrawerState? = null,
    private val warehouses: GetWarehousesResult,
) : BasePresenter(ctx, scope, setting, navigator, page, drawer) {
    private var _lifecycleOwner: LifecycleOwner? = null
    private var _cameraPermission: PermissionState? = null
    private var _preview: MutableState<Preview?> = mutableStateOf(null)
    private var _cameraSelector: MutableState<CameraSelector?> = mutableStateOf(null)
    private var _cameraProvider: MutableState<ProcessCameraProvider?> = mutableStateOf(null)
    private var _barcode = mutableStateOf("")
    private var _scanCount = mutableIntStateOf(0)
    private var _barcodes: State<List<ReservedBarcodeData>?>? = null
    private var _cropSize = mutableStateOf(Animatable(300.dp.value / 5))
    private var _density: Density? = null
    private var _store: BarcodesStore? = null
    private var _initializing = mutableStateOf(true)
    private var _warehouse: MutableState<Warehouse?> = mutableStateOf(null)
    private var _isAutoScan: State<Boolean> = mutableStateOf(false)
    private var _checkingOffline: MutableState<Boolean> = mutableStateOf(false)
    private var _notFound: MutableState<Boolean> = mutableStateOf(false)
    private var _showRaw: MutableState<Boolean> = mutableStateOf(false)
    private var _scanByScanner: MutableState<Boolean> = mutableStateOf(false)
    private var _offline: MutableState<Boolean> = mutableStateOf(true)
    private var _doneRead: MutableState<Boolean> = mutableStateOf(false)
    private var _showCamera = mutableStateOf(true)
    private var _readingFile = mutableStateOf(false)
    private var _items = mutableStateListOf<Item>()
    private var _manualQty = mutableStateOf<Int?>(1)
    private var _captureImage = mutableStateOf<Bitmap?>(null)
    private var _showExternal = mutableStateOf(false)
    val scanByScanner get() = _scanByScanner.value
    val requestPermission: () -> Unit = { _cameraPermission!!.launchPermissionRequest() }
    val updateManualQty: (qty: String) -> Unit = { _manualQty.value = it.toIntOrNull() }
    val captureImage get() = _captureImage
    val manualQty get() = _manualQty
    val showCamera get() = _showCamera
    val isAutoScan get() = _isAutoScan
    val showRaw get() = _showRaw.value
    val offline get() = _offline.value
    val items get() = _items
    val barcode get() = _barcode
    val cameraGranted get() = _cameraPermission?.hasPermission ?: false
    val scanCount get() = _scanCount
    val notFound get() = _notFound.value
    val startNotFound: () -> Unit = { _notFound.value = true }
    val showExternal get() = _showExternal.value
    val startShowExternal: () -> Unit = { _showExternal.value = true }
    val hideExternal: () -> Unit = { _showExternal.value = false }
    val stopNotFound: () -> Unit = {
        _notFound.value = false
        startShowExternal()
    }
    val checkingOffline get() = _checkingOffline.value

    private var _canScan = mutableStateOf(false)
    private var _format = mutableStateOf<Int?>(null)
    override val render: @Composable (content: (() -> Unit)?) -> Unit = {
        _isAutoScan = setting!!.isAutoScanMode.collectAsState(initial = false)
        _offline = remember { mutableStateOf(true) }
        _doneRead = remember { mutableStateOf(false) }
        _readingFile = remember { mutableStateOf(false) }
        _notFound = remember { mutableStateOf(false) }
        _items = remember { mutableStateListOf() }
        _warehouse = remember { mutableStateOf(null) }
        _captureImage = remember { mutableStateOf(null) }
        _manualQty = remember { mutableStateOf(1) }
        _cameraSelector = remember { mutableStateOf(null) }
        _cameraProvider = remember { mutableStateOf(null) }
        _preview = remember { mutableStateOf(null) }
        _showCamera = remember { mutableStateOf(true) }
        _showExternal = remember { mutableStateOf(false) }
        _canScan = remember { mutableStateOf(false) }
        _checkingOffline = remember { mutableStateOf(false) }
        _showRaw = remember { mutableStateOf(false) }
        _scanByScanner = remember { mutableStateOf(false) }
        _barcode = remember { mutableStateOf("") }
        _density = LocalDensity.current
        _cropSize = remember { mutableStateOf(Animatable(300.dp.value / 5)) }
        _lifecycleOwner = LocalLifecycleOwner.current
        _cameraPermission = rememberPermissionState(permission = Manifest.permission.CAMERA)
        _scanCount = remember { mutableIntStateOf(0) }
        _store = BarcodesStore(ctx!!)
        _barcodes = _store!!.codes.collectAsState(initial = null)
        _initializing = remember(_barcodes!!.value)
        { mutableStateOf(_barcodes!!.value == null) }
        val expanded = remember { mutableStateOf(false) }

        DisposableEffect(_cameraProvider.value) {
            onDispose { _cameraProvider.value?.unbindAll() }
        }
//        LaunchedEffect(Unit) {
//            readFile()
//        }
        if (!_canScan.value && !_scanByScanner.value) Column(
            Modifier
                .fillMaxSize()
                .bestBg()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .bestBg(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "To continue, please select a warehouse")
                Spacer(modifier = Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier
                            .clickable(enabled = !_readingFile.value) {
                                expanded.value = !expanded.value
                            }
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = Color.Gray,
                                shape = RoundedCornerShape(5.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = _warehouse.value?.name ?: "Select warehouse")
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "expand-warehouse"
                        )
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val config = LocalConfiguration.current
                        DropdownMenu(
                            expanded = expanded.value,
                            onDismissRequest = { expanded.value = false },
                            modifier = Modifier.width(config.screenWidthDp.dp - 20.dp)
                        ) {
                            repeat(warehouses.warehouses.size) {
                                DropdownMenuItem(
                                    text = { Text(text = warehouses.warehouses[it].name) },
                                    onClick = {
                                        _warehouse.value = warehouses.warehouses[it]
                                        expanded.value = false
                                    })
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = _offline.value, onCheckedChange = {
                        _offline.value = it
                        if (it && !_doneRead.value) readFile()
                    })
                    Text(
                        text = "Offline mode",
                        modifier = Modifier.clickable(
                            interactionSource = NoRippleInteraction(),
                            indication = null,
                            onClick = {
                                _offline.value = !_offline.value
                                if (_offline.value && !_doneRead.value) readFile()
                            })
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Button(
                        onClick = { _canScan.value = true },
                        enabled = _warehouse.value != null && !_readingFile.value,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(Color(0xFF587734))
                    ) { Text(text = "Scan by camera") }
                    Button(
                        onClick = { _scanByScanner.value = true },
                        enabled = _warehouse.value != null && !_readingFile.value,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(Color(0xFF587734))
                    ) { Text(text = "Scan by scanner") }
                }
                val show = remember {
                    mutableStateOf(false)
                }
//                LaunchedEffect(_lines) {
//                    if (_lines.isNotEmpty()) {
//                        show.value = true
//                    }
//                }
                if (show.value) {
                    Dialog(onDismissRequest = { show.value = false }) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .bestBg()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LazyColumn(modifier = Modifier.weight(1f)) {
//                                    items(_lines.size, key = { k -> lines[k] }) {
//                                        Text(text = _items[it].itemNumber)
//                                    }
                                }
                                Button(onClick = { show.value = false }) {
                                    Text(text = "close")
                                }
                            }

                        }
                    }
                }
//                if (_readingFile.value) {
//                    Spacer(modifier = Modifier.height(10.dp))
//                    val dots = remember { mutableStateOf("") }
//                    LaunchedEffect(dots.value) {
//                        delay(100L)
//                        if (dots.value == "...") dots.value = ""
//                        else dots.value += "."
//                    }
//                    Text(text = "Reading file$dots")
//                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bestBg()
            ) {
                Button(
                    onClick = { navigator!!.navigate("codes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        Color(0xFF2C5F88)
                    )
                ) { Text(text = "Check list") }
            }
        } else if (_initializing.value) Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text(text = "Initializing...") }
        else super.render { Scan(this) }
    }

    private fun readFile() {
        val fileName = "conicalhat-items"
        val file = File(ctx!!.filesDir, fileName)
        if (file.exists()) {
            _readingFile.value = true
//            val tempLines = mutableListOf<String>()
            val lines = file.readLines().reversed()
            if (_items.isNotEmpty()) _items.clear()
            for (l in lines) {
                val chunks = l.split(";;").filter { c -> c.isNotEmpty() && c.isNotBlank() }
                    .map { c -> c.trim() }
                val n = if (chunks.isEmpty()) "" else chunks[0]
                val bc = if (chunks.size > 1) chunks[1] else ""
                if (n.isEmpty() && bc.isEmpty()) continue
                _items.add(Item(itemNumber = n, barcode = bc))
            }
//            ctx!!.openFileInput(fileName).bufferedReader().useLines { l ->
//                tempLines.add(l.fold("") { a, b -> "$a\n$b" })
//            }
//            if (tempLines.isNotEmpty()) {
//                val chunks =
//                    tempLines[0].split("\n").filter { s -> s.isNotEmpty() && s.isNotBlank() }
//                _items.addAll(chunks.map { c ->
//                    val sub = c.split(";;")
//                    Item(itemNumber = sub[0], barcode = if (sub[1] == "null") "" else sub[1])
//                })
//            }
            _readingFile.value = false
        }
        _doneRead.value = true
    }

    fun checkItemOffline(code: String): Boolean {
        _checkingOffline.value = true
        val file = File(ctx!!.filesDir, "conicalhat-items")
        if (!file.exists()) {
            _checkingOffline.value = false
            return false
        }
        val tLines = file.readLines()
        if (tLines.isEmpty()) {
            _checkingOffline.value = false
            return false
        }
        for (l in tLines) {
            val chunks =
                l.split(";;").filter { c -> c.isNotEmpty() && c.isNotBlank() }.map { c -> c.trim() }
            val n = if (chunks.isEmpty()) "" else chunks[0]
            val bc = if (chunks.size > 1) chunks[1] else ""
            if (n == code || bc == code) {
                _checkingOffline.value = false
                return true
            }
        }
        _checkingOffline.value = false
        return false
    }

    fun onExternalScanned(
        code: String,
        count: Int = 1,
        onAdded: () -> Unit = {},
        afterDelay: (() -> Unit)? = null
    ) {
        scope?.launch {
            _store?.modify(
                ReservedBarcodeData(
                    code = code,
                    warehouse = _warehouse.value!!.name,
                    count = count
                )
            )
            onAdded()
            if (afterDelay != null) {
                delay(200L)
                afterDelay()
            }
        }
    }

    fun onContinue() {
        _notFound.value = false
        if (_isAutoScan.value) {
            _showCamera.value = true
            _captureImage.value = null
            scope?.launch {
                _store!!.modify(
                    ReservedBarcodeData(
                        code = _barcode.value,
                        warehouse = _warehouse.value!!.name
                    )
                )
                _barcode.value = ""
            }
            return
        }
    }

    fun scanMore() {
        scope?.launch {
            if (_manualQty.value == null) _manualQty.value = 1
            _store!!.modify(
                ReservedBarcodeData(
                    code = _barcode.value,
                    warehouse = _warehouse.value!!.name,
                    count = _manualQty.value ?: 1
                )
            )
            delay(100)
            _barcode.value = ""
            _manualQty.value = 1
            _showCamera.value = true
        }
    }

    fun discard() {
        _barcode.value = ""
        _manualQty.value = 1
        _showCamera.value = true
    }

    fun onCodeDetected(c: String) {
        _barcode.value = c
        if (_offline.value && !checkItemOffline(c)) {
            _notFound.value = true
            _showCamera.value = false
            return
        }
        val count = _barcodes!!.value!!
            .firstOrNull { d -> d.code == c }?.count
        _scanCount.intValue = if (count == null) 1 else count + 1
        if (_isAutoScan.value) {
            try {
                scope?.launch {
                    _store!!.modify(
                        ReservedBarcodeData(
                            code = c,
                            warehouse = _warehouse.value!!.name
                        )
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(ctx!!, e.message, Toast.LENGTH_LONG).show()
            }
        } else _showCamera.value = false
    }

    fun onStartAnalyze(image: Bitmap, format: Int) {
        try {
            _captureImage.value = image
        } catch (e: Exception) {
            Toast.makeText(ctx!!, e.message, Toast.LENGTH_LONG).show()
        }
        _format.value = format
    }
}