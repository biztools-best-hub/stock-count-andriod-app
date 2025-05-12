package com.biztools.stockcount.presentations.pagePresentations

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.core.Animatable
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.biztools.stockcount.api.RestAPI
import com.biztools.stockcount.api.StockApi
import com.biztools.stockcount.models.GetWarehousesResult
import com.biztools.stockcount.models.ItemInfo
import com.biztools.stockcount.models.User
import com.biztools.stockcount.presentations.layoutPresentations.AuthPresenter
import com.biztools.stockcount.stores.SettingStore
import com.biztools.stockcount.stores.UserStore
import com.biztools.stockcount.ui.pages.PriceChecker
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope

@ExperimentalGetImage
@OptIn(ExperimentalPermissionsApi::class)
class PriceCheckPresenter(
    val ctx: Context,
    val scope: CoroutineScope,
    val setting: SettingStore,
    val navigator: NavHostController,
    val page: MutableState<String>,
    val drawer: DrawerState,
    val warehouse: GetWarehousesResult,
    val onUnauth: () -> Unit,
    val onAuth: () -> Unit
)
//    : BasePresenter(ctx, scope, setting, navigator, page, drawer)
{
    private var _cameraPermission: PermissionState? = null
    private var _lifecycleOwner: LifecycleOwner? = null
    private var _user: State<User?> = mutableStateOf(null)
    private var _cameraProvider: ProcessCameraProvider? = null
    private var _cropSize = mutableStateOf(Animatable(300.dp.value / 5))
    private var _captureImage = mutableStateOf<Bitmap?>(null)
    private var _showCamera = mutableStateOf(false)
    private var _format = mutableStateOf<Int?>(null)
    private var _barcode = mutableStateOf("")
    private var _density: Density? = null
    private var _selectedWarehouse = mutableStateOf<String?>(null)
    private var _currentItem = mutableStateOf<ItemInfo?>(null)
    private var _checking = mutableStateOf(false)
    private var _notFoundError = mutableStateOf(false)
    val captureImage get() = _captureImage.value
    val currentItem get() = _currentItem.value
    val requestPermission: () -> Unit = { _cameraPermission!!.launchPermissionRequest() }
    val showCamera get() = _showCamera.value
    val barcode get() = _barcode.value
    val cameraGranted get() = _cameraPermission?.hasPermission ?: false
    val selectedWarehouse get() = _selectedWarehouse.value
    val checking get() = _checking.value
    fun resetCurrentItem() {
        _currentItem.value = null;
    }

    fun updateBarcode(code: String) {
        _barcode.value = code
    }

    fun onCodeDetected(code: String, dev: String) {
        _barcode.value = code
        _showCamera.value = false
        onCheckPrice(code, dev)
    }

    fun onStartAnalyze(image: Bitmap, format: Int) {
        try {
            _captureImage.value = image
        } catch (e: Exception) {
            Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
        }
        _format.value = format
    }

    val notFound get() = _notFoundError.value
    fun closeError() {
        _notFoundError.value = false
        _barcode.value = ""
    }

    @Composable
    fun Render() {
        _captureImage = remember { mutableStateOf(null) }
        _showCamera = remember { mutableStateOf(false) }
        _cropSize = remember { mutableStateOf(Animatable(300.dp.value / 5)) }
        _lifecycleOwner = LocalLifecycleOwner.current
        _cameraPermission = rememberPermissionState(permission = Manifest.permission.CAMERA)
        _selectedWarehouse = remember { mutableStateOf(null) }
        _notFoundError = remember { mutableStateOf(false) }
        _density = LocalDensity.current
        _barcode = remember { mutableStateOf("") }
        _format = remember { mutableStateOf(null) }
        _currentItem = remember { mutableStateOf(null) }
        _checking = remember { mutableStateOf(false) }
        val store = UserStore(ctx)
        _user = store.user.collectAsState(initial = null)
        DisposableEffect(_cameraProvider) {
            onDispose { _cameraProvider?.unbindAll() }
        }
        AuthPresenter(
            ctx = ctx,
            scope = scope,
            darkTheme = false,
            navigator = navigator,
            drawer = drawer,
            onAuth = onAuth,
        ) { PriceChecker(presenter = this) }.Initialize()
    }

    val onQrClick: () -> Unit = { _showCamera.value = true }

    val closeCamera: () -> Unit = { _showCamera.value = false }

    val selectWarehouse: (name: String) -> Unit = { _selectedWarehouse.value = it }
    fun gotoAddItem() {
        navigator.navigate("add-item?code=$barcode&fromRoute=${page.value}")
        _notFoundError.value = false
    }

    private fun onCheckPrice(code: String, dev: String) {
        _checking.value = true
        _currentItem.value = null
        try {
            val api = RestAPI.create<StockApi>(_user.value?.token, deviceId = dev)
            val call = api.checkItemInfo(_selectedWarehouse.value!!, code)
            RestAPI.execute(call, scope,
                onSuccess = { r ->
                    _currentItem.value = r
                    _checking.value = false
                    _barcode.value = ""
                },
                onError = { e ->
                    _checking.value = false
                    if (e.message == "not found") {
                        _notFoundError.value = true
                    } else if (e.message?.startsWith("unauth") == true) {
                        onUnauth()
                    } else Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
                })
        } catch (e: Exception) {
            _checking.value = false
            Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
        }
    }
}