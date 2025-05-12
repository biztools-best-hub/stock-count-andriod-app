package com.biztools.stockcount.ui.extensions

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.borderBottom(color: Color = Color.Gray, stroke: Dp = 1.dp) = drawBehind {
    val width = density.run { stroke.toPx() }
    drawLine(
        color,
        Offset(0f, this.size.height),
        Offset(this.size.width, this.size.height),
        width
    )
}

fun Modifier.borderTop(color: Color = Color.Gray, stroke: Dp = 1.dp) = drawBehind {
    val width = density.run { stroke.toPx() }
    drawLine(
        color,
        Offset(0f, 0f),
        Offset(this.size.width, 0f),
        width
    )
}

fun Modifier.borderLeft(color: Color = Color.Gray, stroke: Dp = 1.dp) = drawBehind {
    val width = density.run { stroke.toPx() }
    drawLine(
        color,
        Offset(0f, 0f),
        Offset(0f, this.size.height),
        width
    )
}

fun Modifier.borderRight(color: Color = Color.Gray, stroke: Dp = 1.dp) = drawBehind {
    val width = density.run { stroke.toPx() }
    drawLine(
        color,
        Offset(this.size.width, 0f),
        Offset(this.size.width, this.size.height),
        width
    )
}