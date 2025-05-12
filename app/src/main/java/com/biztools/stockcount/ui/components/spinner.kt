package com.biztools.stockcount.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun CircularLoading(modifier: Modifier = Modifier) {
    val count = 8
    val infiniteTransit = rememberInfiniteTransition(label = "spinner")
    val angle by infiniteTransit.animateFloat(
        0f,
        count.toFloat(),
        infiniteRepeatable(
            tween(count * 100, easing = LinearEasing),
            RepeatMode.Restart
        ), label = "spinner"
    )
    Canvas(modifier = modifier.size(48.dp)) {
        val width = size.width
        val height = size.height
        val w = size.width / 4
        val h = size.height / 4
        val corner = w.coerceAtMost(h) / 2
        for (n in 0..360 step 360 / count) {
            rotate(n.toFloat()) {
                drawRoundRect(
                    color = Color.LightGray.copy(alpha = .7f),
                    topLeft = Offset(width - w, (height - h) / 2),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(corner, corner)
                )
            }
        }
        val co = 360f / count
        for (n in 1..4) {
            rotate((angle.toInt() + n) * co) {
                drawRoundRect(
                    color = Color.Gray.copy(alpha = (.2f + .2f * n).coerceIn(0f, 1f)),
                    topLeft = Offset(width - w, (height - h) / 2),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(corner, corner)
                )
            }
        }
    }
}