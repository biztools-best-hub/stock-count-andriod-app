package com.biztools.stockcount.ui.layouts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.biztools.stockcount.presentations.layoutPresentations.BottomBarPresenter
import com.biztools.stockcount.presentations.layoutPresentations.ScaffoldPresenter
import com.biztools.stockcount.presentations.layoutPresentations.TopBarPresenter

@Composable
fun MainScaffold(
    presenter: ScaffoldPresenter,
    topBarPresenter: TopBarPresenter,
    bottomBarPresenter: BottomBarPresenter
) {
    Scaffold(
        topBar = { topBarPresenter.Render() },
        bottomBar = { bottomBarPresenter.Render() }) {
        Surface(
            Modifier
                .fillMaxSize()
                .padding(it)
        ) { presenter.Content() }
        presenter.RenderQR()
    }
}