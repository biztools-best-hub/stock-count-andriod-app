package com.biztools.stockcount

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.biztools.stockcount.presentations.layoutPresentations.MainPresenter
import com.biztools.stockcount.stores.SettingStore
import com.biztools.stockcount.stores.currentInteractions
import com.biztools.stockcount.stores.previousInteractions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val scope = rememberCoroutineScope()
            val setting = SettingStore(this)
            val navigator = rememberNavController()
            val page = remember(navigator.currentDestination?.route) {
                mutableStateOf(navigator.currentDestination?.route ?: "menu")
            }
            currentInteractions = remember { mutableIntStateOf(0) }
            previousInteractions = remember { mutableIntStateOf(0) }
            navigator.addOnDestinationChangedListener { _, dest, _ ->
                currentInteractions.intValue++
                page.value = dest.route ?: "menu"
            }
            val drawer = rememberDrawerState(initialValue = DrawerValue.Closed)
            MainPresenter(this, scope, setting, navigator, page, drawer).Render()
        }
    }
}