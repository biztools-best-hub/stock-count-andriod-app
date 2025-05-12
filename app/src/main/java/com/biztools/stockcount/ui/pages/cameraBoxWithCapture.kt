package com.biztools.stockcount.ui.pages

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.Executors

@Composable
fun CameraBoxWithCapture(ctx: Context, scope: CoroutineScope) {
    val executor = Executors.newSingleThreadExecutor()
    val cameraFuture = remember { ProcessCameraProvider.getInstance(ctx) }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    val cameraProvider = cameraFuture.get()
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(null)
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
                            p.setSurfaceProvider(it.surfaceProvider)
                        }
                    try {
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                        )
                    } catch (_: Exception) {
                    }
                }, ContextCompat.getMainExecutor(ctx))
            }
        )
        Button(onClick = {
            imageCapture?.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
                @OptIn(ExperimentalGetImage::class)
                override fun onCaptureSuccess(image: ImageProxy) {
                    if (image.image == null) return
//                    textRecognitionViewModel.scan(
//                        InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees)
//                    )
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(ctx, exception.message, Toast.LENGTH_LONG)
                        .show()
                }
            })
        }) {}
    }
}