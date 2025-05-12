package com.biztools.stockcount.ui.utilities

import android.graphics.ImageFormat
import java.util.Calendar

val Calendar.customDisplay
    get() = "${this.get(Calendar.DATE)}/${this.get(Calendar.MONTH)}/${
        this.get(Calendar.YEAR)
    } ${this.get(Calendar.HOUR)}:${this.get(Calendar.MINUTE)}"
val Int.imageFormatAsString
    get() = when (this) {
        ImageFormat.NV21 -> "nv21"
        ImageFormat.DEPTH16 -> "depth16"
        ImageFormat.DEPTH_JPEG -> "depth-jpg"
        ImageFormat.HEIC -> "heic"
        ImageFormat.JPEG -> "jpeg"
        ImageFormat.DEPTH_POINT_CLOUD -> "depth-point-cloud"
        ImageFormat.FLEX_RGBA_8888 -> "flex-rgba-8888"
        ImageFormat.FLEX_RGB_888 -> "flex-rgb-888"
        ImageFormat.NV16 -> "nv16"
        ImageFormat.PRIVATE -> "private"
        ImageFormat.RAW10 -> "raw10"
        ImageFormat.RAW12 -> "raw12"
        ImageFormat.RAW_PRIVATE -> "raw-private"
        ImageFormat.RAW_SENSOR -> "raw-sensor"
        ImageFormat.UNKNOWN -> "unknown"
        ImageFormat.RGB_565 -> "rgb565"
        ImageFormat.Y8 -> "y8"
        ImageFormat.YCBCR_P010 -> "ycbcr-p010"
        ImageFormat.YUV_420_888 -> "yuv-420-888"
        ImageFormat.YUV_422_888 -> "yuv-422-888"
        ImageFormat.YUV_444_888 -> "yuv-444-888"
        ImageFormat.YUY2 -> "yuv2"
        else -> "yuv12"
    }