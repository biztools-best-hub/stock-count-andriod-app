package com.biztools.stockcount.ui.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import java.io.ByteArrayOutputStream

val Image.asBitmap get():Bitmap = extractBitmapFromImage(this)

private fun extractBitmapFromImage(img: Image): Bitmap {
    val yBuf = img.planes[0].buffer
    val vBuf = img.planes[2].buffer
    val ySize = yBuf.remaining()
    val vSize = vBuf.remaining()
    val n21 = ByteArray(ySize + vSize)
    yBuf.get(n21, 0, ySize)
    vBuf.get(n21, ySize, vSize)
    val yImage = YuvImage(n21, ImageFormat.NV21, img.width, img.height, null)
    val out = ByteArrayOutputStream()
    yImage.compressToJpeg(Rect(0, 0, yImage.width, yImage.height), 50, out)
    val bytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}