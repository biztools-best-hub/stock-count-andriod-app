package com.biztools.stockcount.ui.layouts

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.biztools.stockcount.R
import com.biztools.stockcount.presentations.layoutPresentations.DrawerPresenter
import com.biztools.stockcount.stores.SecurityStore
import com.biztools.stockcount.stores.UserStore
import com.biztools.stockcount.ui.components.CircularLoading
import com.biztools.stockcount.ui.extensions.bestBg
import com.biztools.stockcount.ui.theme.BackgroundAccent
import com.biztools.stockcount.ui.theme.BackgroundDim
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@Composable
fun Drawer(presenter: DrawerPresenter) {
    val dev = SecurityStore(presenter.ctx).device.collectAsState(initial = null)
    val user = UserStore(presenter.ctx).user.collectAsState(initial = null)
    ModalNavigationDrawer(
        gesturesEnabled = presenter.drawer.isOpen,
        drawerState = presenter.drawer,
        drawerContent = {
            ModalDrawerSheet(
                Modifier
                    .width(300.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                BackgroundAccent, BackgroundDim
                            )
                        )
                    ),
                drawerShape = RoundedCornerShape(0.dp),
                windowInsets = WindowInsets(
                    left = 10.dp,
                    right = 10.dp,
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 10.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 10.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painterResource(id = R.drawable.avatar),
                                contentDescription = "user",
                                modifier = Modifier.size(60.dp),
                            )
                            if (user.value == null || presenter.isUnauth) {
                                TextButton(onClick = { presenter.nowLogin() }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "LOGIN", fontSize = 20.sp)
                                        Icon(
                                            imageVector = Icons.Filled.ArrowForward,
                                            contentDescription = "login"
                                        )
                                    }
                                }
                            } else Column {
                                Text(text = user.value!!.username, fontSize = 18.sp)
                                Button(
                                    onClick = { presenter.onLogout() },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(24.dp)
                                ) { Text(text = "Logout", fontSize = 12.sp) }
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.best_logo),
                                contentDescription = "logo",
                                modifier = Modifier.height(16.dp)
                            )
                            Text(text = "Biztools Enterprise Solution Technology", fontSize = 9.sp)
                        }
                    }
                }
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(10.dp))
                presenter.RenderItem(false,
                    {
                        Row(
                            Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) { Text(text = "Add Item") }
                    }, {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "add-item",
                        )
                    }, {
                        presenter.navigator.navigate("add-item")
                        presenter.scope.launch { presenter.drawer.close() }
                    }
                )
                presenter.RenderItem(false,
                    {
                        Row(
                            Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) { Text(text = "PO Config") }
                    }, {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "po-config",
                            modifier = Modifier.size(20.dp)
                        )
                    }, {
                        presenter.navigator.navigate("config")
                        presenter.scope.launch { presenter.drawer.close() }
                    }
                )
                presenter.RenderItem(false,
                    {
                        Row(
                            Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) { Text(text = "Download items") }
                    }, {
                        Icon(
                            painterResource(id = R.drawable.save),
                            contentDescription = "download",
                            modifier = Modifier.size(20.dp)
                        )
                    }, {
                        presenter.scope.launch {
                            presenter.onDownload(dev.value ?: "")
                            presenter.drawer.close()
                        }
                    }
                )
                presenter.RenderItem(false,
                    {
                        Row(
                            Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Scan Mode")
                            Text(text = if (presenter.isAutoScan.value) "Auto" else "Manual")
                        }
                    }, {
                        Icon(
                            painterResource(id = R.drawable.scan),
                            contentDescription = "scan mode",
                            modifier = Modifier.size(20.dp)
                        )
                    }, { presenter.toggleScanMode() }
                )
                presenter.RenderItem(
                    false,
                    { Text(text = "Exit") }, {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "exit"
                        )
                    }, {
                        presenter.closeDrawer(callBackBefore = {
                            (presenter.ctx as Activity).finish()
                            exitProcess(0)
                        })
                    })
            }
        }) {
        presenter.Content()
    }
    if (presenter.showLogin) presenter.LoginBox(dev.value ?: "")
    if (presenter.downloading || presenter.failedDownload || presenter.downloadSuccess) Dialog(
        onDismissRequest = {
            presenter.closeLoading()
        }) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .widthIn(200.dp)
                    .heightIn(min = 100.dp)
                    .bestBg()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (presenter.downloading) {
                        CircularLoading(modifier = Modifier.size(30.dp))
                        Text(text = "Downloading")
                    } else if (presenter.failedDownload) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "failed",
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFFE48A05)
                        )
                        Text(text = "Download failed")
                        Text(text = "Try again?")
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Button(
                                onClick = { presenter.onDownload(dev.value ?: "") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(Color(0xFF4B6928))
                            ) { Text(text = "Yes") }
                            Button(
                                onClick = { presenter.closeLoading() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(Color.Gray)
                            ) { Text(text = "No") }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "complete",
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFF53742D)
                        )
                        Text(text = "Download complete")
                        Button(
                            onClick = { presenter.closeLoading() },
                            colors = ButtonDefaults.buttonColors(
                                Color(0xFF265981)
                            )
                        ) { Text(text = "OK") }
                    }
                }
            }
        }
    }
}