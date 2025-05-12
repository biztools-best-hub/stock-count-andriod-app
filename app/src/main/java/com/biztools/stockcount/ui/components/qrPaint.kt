package com.biztools.stockcount.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlin.random.Random

@Composable
fun qRPaint(givenSize: Dp): BitmapPainter {
    val density = LocalDensity.current
    val size = with(density) { givenSize.roundToPx() }
    val padding = with(density) { 1.dp.roundToPx() }
    var bitmap by remember {
        mutableStateOf(ImageBitmap(size, size))
    }
    LaunchedEffect(true) {
        val writer = QRCodeWriter()
        val hints = mutableMapOf<EncodeHintType, Any?>().apply {
            this[EncodeHintType.MARGIN] = padding
        }
        val matrix = try {
            writer.encode(
                Random.nextInt(from = 100000, until = 999999).toString(),
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )
        } catch (_: Exception) {
            null
        }
        val matrixWidth = matrix?.width ?: size
        val matrixHeight = matrix?.height ?: size
        val innerBitmap = ImageBitmap(matrixWidth, matrixHeight).asAndroidBitmap()
        for (m in 0 until matrixWidth) {
            for (n in 0 until matrixHeight) {
                val altColor = matrix?.get(m, n) ?: false
                val color = if (altColor) Color.Black else Color.White
                innerBitmap[m, n] = color.toArgb()
            }
        }
        bitmap = innerBitmap.asImageBitmap()
    }
    return remember(bitmap) {
        BitmapPainter(bitmap)
    }
}