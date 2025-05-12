package com.biztools.stockcount.ui.pages

//import androidx.compose.material3.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.biztools.stockcount.R
import com.biztools.stockcount.presentations.pagePresentations.MenuPresenter
import com.biztools.stockcount.ui.extensions.bestBg
import com.biztools.stockcount.ui.theme.White100

@Composable
fun Menu(presenter: MenuPresenter) {
    val darkColor = Color(0xFF414141)
    val colors = CardDefaults.cardColors(
        containerColor = if (presenter.isDarkTheme)
            darkColor else White100
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .bestBg(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .width(290.dp)
                .height(320.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable { presenter.toCheckPrice() },
                    colors = colors,
                    elevation = CardDefaults.cardElevation(3.dp),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF5BCEC4))
                            .padding(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.check_info_img),
                            modifier = Modifier.width(60.dp),
                            contentDescription = "check-price"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Check Item", textAlign = TextAlign.Center)
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable { presenter.toScan() },
                    colors = colors,
                    elevation = CardDefaults.cardElevation(3.dp),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF83A9EC))
                            .padding(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.scan_img),
                            modifier = Modifier.width(60.dp),
                            contentDescription = "scan-qr"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Count Stock", textAlign = TextAlign.Center)
                    }
                }

            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable { presenter.toPrintLabel() },
                    colors = colors,
                    elevation = CardDefaults.cardElevation(3.dp),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF8EB660))
                            .padding(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.print_label_img),
                            modifier = Modifier.width(50.dp),
                            contentDescription = "print-label"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Print Label", textAlign = TextAlign.Center)
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable { presenter.toPo() },
                    colors = colors,
                    elevation = CardDefaults.cardElevation(3.dp),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFDF9C81))
                            .padding(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.po_img),
                            modifier = Modifier.width(50.dp),
                            contentDescription = "po"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Purchase Order", textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
    presenter.content()
}