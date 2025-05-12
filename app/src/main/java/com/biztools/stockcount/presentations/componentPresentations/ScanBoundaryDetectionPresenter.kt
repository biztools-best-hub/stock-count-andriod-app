package com.biztools.stockcount.presentations.componentPresentations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.biztools.stockcount.ui.components.ScanBoundaryDetector

//class ScanBoundaryDetectionPresenter {
//    private var _size = mutableStateOf(30.dp)
//    val size get() = _size
//    val render: @Composable (modifier: Modifier, width: Dp) -> Unit
//        get() = { m, w ->
//            _size = remember(w) { mutableStateOf(w / 5) }
//            ScanBoundaryDetector(presenter = this, m)
//        }
//}