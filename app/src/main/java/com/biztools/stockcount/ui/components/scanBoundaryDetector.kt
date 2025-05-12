package com.biztools.stockcount.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.biztools.stockcount.ui.extensions.borderBottom
import com.biztools.stockcount.ui.extensions.borderLeft
import com.biztools.stockcount.ui.extensions.borderRight
import com.biztools.stockcount.ui.extensions.borderTop

@Composable
fun ScanBoundaryDetector(width: Dp, modifier: Modifier = Modifier) {
    val size = remember(width) { mutableStateOf(width / 5) }
    val height by remember(size.value) { mutableStateOf(size.value * 3) }
    Column(
        modifier = modifier
            .width(width)
            .height(height)
    ) {
        repeat(3) { i ->
            Row(Modifier.weight(1f)) {
                repeat(5) { j ->
                    val m = when ((i * 5) + (j + 1)) {
                        1 -> Modifier
                            .size(size.value)
                            .borderTop(color = Color.White, stroke = 2.dp)
                            .borderLeft(color = Color.White, stroke = 2.dp)

                        3 -> Modifier
                            .size(size.value)
                            .borderTop(color = Color.White, stroke = 2.dp)
                            .borderTop(color = Color.White, stroke = 2.dp)

                        5 -> Modifier
                            .size(size.value)
                            .borderTop(color = Color.White, stroke = 2.dp)
                            .borderRight(color = Color.White, stroke = 2.dp)

                        11 -> Modifier
                            .size(size.value)
                            .borderBottom(color = Color.White, stroke = 2.dp)
                            .borderLeft(color = Color.White, stroke = 2.dp)

                        13 -> Modifier
                            .size(size.value)
                            .borderBottom(color = Color.White, stroke = 2.dp)

                        15 -> Modifier
                            .size(size.value)
                            .borderBottom(color = Color.White, stroke = 2.dp)
                            .borderRight(color = Color.White, stroke = 2.dp)

                        else -> Modifier.size(size.value)
                    }
                    Box(modifier = m)
                }
            }
        }
    }
}