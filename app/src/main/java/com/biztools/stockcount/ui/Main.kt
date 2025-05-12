package com.biztools.stockcount.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.biztools.stockcount.presentations.layoutPresentations.MainPresenter
import com.biztools.stockcount.ui.theme.CodeScannerTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun Main(presenter: MainPresenter) {
    val systemUiController = rememberSystemUiController()
    CodeScannerTheme(false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { presenter.Content() }
        }
    }
    DisposableEffect(systemUiController) {
        systemUiController.isNavigationBarContrastEnforced = false
        systemUiController.navigationBarDarkContentEnabled = true
        systemUiController.statusBarDarkContentEnabled = true
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = true,
            isNavigationBarContrastEnforced = false
        ) { presenter.scrim(it) }
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = true,
            navigationBarContrastEnforced = false
        ) { presenter.scrim(it) }
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true
        ) { presenter.scrim(it) }
        onDispose { }
    }
}