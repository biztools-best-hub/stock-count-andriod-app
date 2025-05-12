package com.biztools.stockcount.ui.pages

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaActionSound
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.biztools.stockcount.ui.extensions.asBitmap
import com.biztools.stockcount.ui.extensions.borderBottom
import com.biztools.stockcount.ui.extensions.borderLeft
import com.biztools.stockcount.ui.extensions.borderRight
import com.biztools.stockcount.ui.extensions.borderTop
import com.biztools.stockcount.ui.utilities.BarcodeAnalyzer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraBox(
    ctx: Context,
    scope: CoroutineScope,
    onCodeDetected: (v: String) -> Unit,
    onStartAnalyze: (img: Bitmap, n: Int) -> Unit,
    caption: (@Composable () -> Unit)? = null
) {
    val executor = Executors.newSingleThreadExecutor()
    val cameraFuture = remember { ProcessCameraProvider.getInstance(ctx) }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    val cameraProvider = cameraFuture.get()
    val cropSize = remember { mutableStateOf(Animatable(300.dp.value / 5)) }
    val density = LocalDensity.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val buildImageAnalysis: (executor: ExecutorService) -> ImageAnalysis = {
        val shutterSound = MediaActionSound()
        val barcodeAnalyzer =
            BarcodeAnalyzer(onBarcodeDetected = { codes, w ->
                scope.launch {
                    cropSize.value.animateTo(
                        targetValue = (with(density) { w.toDp() }.value) / 5,
                        animationSpec = tween(100, easing = LinearEasing)
                    )
                    codes.forEach { code ->
                        code.rawValue?.let {
                            try {
                                shutterSound.play(MediaActionSound.SHUTTER_CLICK)
                                onCodeDetected(it)
                            } catch (ex: Exception) {
                                Toast.makeText(ctx, ex.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    cropSize.value.animateTo(
                        targetValue = 300.dp.value / 5,
                        animationSpec = tween(100, easing = LinearEasing)
                    )
                }
            }, onFailed = {
                Toast.makeText(ctx, it.message, Toast.LENGTH_LONG).show()
            }, onStartAnalyze = {
                onStartAnalyze(it.mediaImage!!.asBitmap, it.format)
            })
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also { a -> a.setAnalyzer(it, barcodeAnalyzer) }
    }
    LaunchedEffect(true) { cameraProvider.unbindAll() }
    DisposableEffect(true) { onDispose { cameraProvider.unbindAll() } }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                PreviewView(it).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = {
                cameraFuture.addListener({
                    val preview = Preview.Builder().build()
                        .also { p ->
                            p.surfaceProvider = it.surfaceProvider
                        }
                    try {
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            buildImageAnalysis(executor)
                        )
                    } catch (_: Exception) {
                    }
                }, ContextCompat.getMainExecutor(ctx))
            }
        )
        if (caption != null) caption()
        Column(
            modifier = Modifier
                .width((cropSize.value.value * 5).dp)
                .height((cropSize.value.value * 3).dp)
                .align(Alignment.Center)
        ) {
            repeat(3) { i ->
                Row(Modifier.weight(1f)) {
                    repeat(5) { j ->
                        val m = when ((i * 5) + (j + 1)) {
                            1 -> Modifier
                                .size(cropSize.value.value.dp)
                                .borderTop(color = Color.White, stroke = 2.dp)
                                .borderLeft(color = Color.White, stroke = 2.dp)

                            3 -> Modifier
                                .size(cropSize.value.value.dp)
                                .borderTop(color = Color.White, stroke = 2.dp)
                                .borderTop(color = Color.White, stroke = 2.dp)

                            5 -> Modifier
                                .size(cropSize.value.value.dp)
                                .borderTop(color = Color.White, stroke = 2.dp)
                                .borderRight(color = Color.White, stroke = 2.dp)

                            11 -> Modifier
                                .size(cropSize.value.value.dp)
                                .borderBottom(color = Color.White, stroke = 2.dp)
                                .borderLeft(color = Color.White, stroke = 2.dp)

                            13 -> Modifier
                                .size(cropSize.value.value.dp)
                                .borderBottom(color = Color.White, stroke = 2.dp)

                            15 -> Modifier
                                .size(cropSize.value.value.dp)
                                .borderBottom(color = Color.White, stroke = 2.dp)
                                .borderRight(color = Color.White, stroke = 2.dp)

                            else -> Modifier.size(cropSize.value.value.dp)
                        }
                        Box(modifier = m)
                    }
                }
            }
        }
    }
}