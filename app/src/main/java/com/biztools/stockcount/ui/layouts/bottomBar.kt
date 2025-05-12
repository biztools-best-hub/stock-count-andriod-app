package com.biztools.stockcount.ui.layouts

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biztools.stockcount.presentations.layoutPresentations.BottomBarPresenter
import com.biztools.stockcount.ui.extensions.customShadow
import com.biztools.stockcount.ui.theme.BackgroundDim

@Composable
fun BottomBar(presenter: BottomBarPresenter) {
    NavigationBar(
        windowInsets = WindowInsets(
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        ),
        containerColor = BackgroundDim,
        tonalElevation = 0.dp,
        modifier = Modifier
            .height(
                WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding() + 40.dp
            )
            .customShadow(x = 0f, y = -1f, alpha = .2f)
    ) {
        presenter.renderItem(
            this,
            presenter.page!!.value == "menu",
            { Text(text = "Home", fontWeight = FontWeight.Light) },
            { Icon(imageVector = Icons.Filled.Home, contentDescription = "home") },
            { presenter.navigateTo("menu") })
//        if (presenter.page!!.value == "scan") presenter.renderItem(
//            this,
//            presenter.page!!.value == "codes",
//            { Text(text = "Unsaved Count", fontWeight = FontWeight.Light) },
//            {
//                Icon(
//                    painter = painterResource(R.drawable.unsaved_count),
//                    contentDescription = "codes",
//                    modifier = Modifier.size(20.dp)
//                )
//            },
//            { presenter.navigateTo("codes") })
    }
}