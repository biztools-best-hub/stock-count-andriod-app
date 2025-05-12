package com.biztools.stockcount.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
    CodeScannerTheme(presenter.isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { presenter.content() }
        }
    }
    DisposableEffect(systemUiController, presenter.isDarkTheme) {
        systemUiController.isNavigationBarContrastEnforced = false
        systemUiController.navigationBarDarkContentEnabled = !presenter.isDarkTheme
        systemUiController.statusBarDarkContentEnabled = !presenter.isDarkTheme
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = !presenter.isDarkTheme,
            isNavigationBarContrastEnforced = false
        ) { presenter.scrim(it) }
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = !presenter.isDarkTheme,
            navigationBarContrastEnforced = false
        ) { presenter.scrim(it) }
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = !presenter.isDarkTheme
        ) { presenter.scrim(it) }
        onDispose { }
    }
}