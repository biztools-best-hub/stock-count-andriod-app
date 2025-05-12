package com.biztools.stockcount.ui.components

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import kotlin.math.floor

@Composable
fun barcodePaint(givenSize: Dp, content: String): BitmapPainter {
    val density = LocalDensity.current
    val size = with(density) { givenSize.roundToPx() }
    val height = with(density) { floor(givenSize.roundToPx() * 0.4).toInt() }
    var bitmap by remember { mutableStateOf(ImageBitmap(size, height)) }
    LaunchedEffect(true) {
        val writer = MultiFormatWriter()
        val matrix = try {
            writer.encode(
                content,
                BarcodeFormat.CODE_128,
                size,
                height
            )
        } catch (_: Exception) {
            null
        }
        val matrixWidth = matrix?.width ?: size
        val matrixHeight = matrix?.height ?: height
        val pixels = IntArray(matrixHeight * matrixWidth)
        for (m in 0 until matrixWidth) {
            for (n in 0 until matrixHeight) {
                pixels[n * matrixWidth + m] =
                    if (matrix?.get(m, n) == true) Color.Black.toArgb()
                    else Color.White.toArgb()
            }
        }
        val innerBitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
        innerBitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
        bitmap = innerBitmap.asImageBitmap()
    }
    return remember(bitmap) { BitmapPainter(bitmap) }
}