package com.biztools.stockcount.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biztools.stockcount.R
import com.biztools.stockcount.presentations.pagePresentations.ReservedCodesPresenter
import com.biztools.stockcount.stores.SecurityStore
import com.biztools.stockcount.ui.extensions.bestBg
import com.biztools.stockcount.ui.extensions.borderBottom
import com.biztools.stockcount.ui.extensions.customShadow
import com.biztools.stockcount.ui.utilities.customDisplay
import java.util.Calendar

data class TempItem(val code: String, val count: Int, val timeStamp: Calendar)
data class ItemGroup(val warehouse: String, val items: List<TempItem>)

@Composable
fun ReservedCodes(presenter: ReservedCodesPresenter) {
    val dev = SecurityStore(presenter.ctx).device.collectAsState(initial = "")
    val codes = presenter.barcodes!!.value!!.groupBy { g -> g.warehouse }
        .filter { g -> g.value.isNotEmpty() }
        .map { g ->
            ItemGroup(
                warehouse = g.value[0].warehouse,
                items = g.value.map { itm ->
                    TempItem(
                        code = itm.code,
                        count = itm.count,
                        timeStamp = itm.timestamp
                    )
                }
            )
        }
    val warehouses = remember { codes.toMutableList() }
//    val threshold = remember { mutableFloatStateOf(0f) }
    Column(
        Modifier
            .fillMaxSize()
            .bestBg()
    ) {
        if (presenter.barcodes!!.value!!.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .weight(1f)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.no_codes),
                            contentDescription = "no codes",
                            modifier = Modifier.height(200.dp)
                        )
                        Text(
                            text = "No scanned item remains in stock",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bestBg()
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Button(
                        onClick = { presenter.navigator.navigate("scan") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            Color(0xFF2C5F88)
                        )
                    ) { Text(text = "Scan") }
                }
            }
        } else LazyColumn(
            Modifier
                .weight(1f)
                .background(Color.White)
                .padding(8.dp),
        ) {
            items(
                count = warehouses.size,
                key = { ii -> warehouses[ii].warehouse }
            ) {
                Column(
                    modifier = Modifier
                        .padding(2.dp)
                        .weight(1f)
                        .customShadow(
                            alpha = 0.2f,
                            x = 1f,
                            y = 1f,
                            corner = with(LocalDensity.current) { 6.dp.toPx() })
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFFFFF))
                ) {
                    Text(
//                        text = warehouses[it].warehouse + " ${threshold.floatValue}",
                        text = warehouses[it].warehouse,
                        modifier = Modifier
                            .borderBottom(Color(0xFFD5D4D4), (0.3).dp)
                            .fillMaxWidth()
                            .padding(5.dp)
                    )
                    repeat(warehouses[it].items.size) { i ->
                        val modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                        Column(
                            modifier.padding(5.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = warehouses[it].items[i].code)
                                Text(text = "x ${warehouses[it].items[i].count}")
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = warehouses[it].items[i].timeStamp.customDisplay,
                                    fontSize = 10.sp
                                )
                                Icon(imageVector = Icons.Filled.Delete, "delete", Modifier
                                    .clickable {
                                        val temp = warehouses.map { w ->
                                            ItemGroup(
                                                warehouse = w.warehouse,
                                                items = if (w.warehouse != warehouses[it].warehouse) w.items else w.items.filter { itm -> itm.code != warehouses[it].items[i].code }
                                            )
                                        }
                                        warehouses.clear()
                                        warehouses.addAll(temp)
                                        presenter.resetCodes(temp)
                                    }
                                    .size(14.dp), tint = Color.Red)
                            }
                        }
//                        val state = rememberDismissState(
//                            initialValue = DismissValue.Default,
//                            confirmValueChange = { d ->
//                                var wiped =
//                                    d == DismissValue.DismissedToEnd || d == DismissValue.DismissedToStart
//                                wiped = wiped && threshold.floatValue > 0.7f
//                                if (wiped) {
//                                    val temp = warehouses.map { w ->
//                                        ItemGroup(
//                                            warehouse = w.warehouse,
//                                            items = if (w.warehouse != warehouses[it].warehouse) w.items else w.items.filter { itm -> itm.code != warehouses[it].items[i].code }
//                                        )
//                                    }
//                                    warehouses.clear()
//                                    warehouses.addAll(temp)
//                                    presenter.resetCodes(temp)
//                                }
//                                wiped
//                            })
//                        LaunchedEffect(state.progress) {
//                            threshold.floatValue = state.progress
//                        }
//                        SwipeToDismiss(
//                            state = state,
//                            directions = setOf(
//                                DismissDirection.StartToEnd,
//                                DismissDirection.EndToStart
//                            ),
//                            background = {
//                                if (state.dismissDirection == null) return@SwipeToDismiss
//                                Box(
//                                    modifier = Modifier
//                                        .fillMaxSize()
//                                        .background(Color.Red)
//                                )
//                            },
//                            dismissContent = {
//                                Column(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .background(Color.White)
//                                        .padding(5.dp),
//                                    verticalArrangement = Arrangement.spacedBy(5.dp)
//                                ) {
//                                    Row(
//                                        Modifier.fillMaxWidth(),
//                                        horizontalArrangement = Arrangement.SpaceBetween
//                                    ) {
//                                        Text(text = warehouses[it].items[i].code)
//                                        Text(text = "x ${warehouses[it].items[i].count}")
//                                    }
//                                    Text(
//                                        text = warehouses[it].items[i].timeStamp.customDisplay,
//                                        fontSize = 10.sp
//                                    )
//                                }
//                            })
                    }
                }
            }
        }
        if (presenter.barcodes!!.value!!.isNotEmpty()) Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Button(
                onClick = { presenter.clearData {} },
                colors = ButtonDefaults.buttonColors(Color.Gray),
                modifier = Modifier.weight(1f)
            )
            { Text(text = "Clear", fontSize = 10.sp) }
            Button(
                onClick = { presenter.navigator.navigate("scan") },
                colors = ButtonDefaults.buttonColors(Color(0xFF313C7E)),
                modifier = Modifier.weight(1f)
            )
            { Text(text = "Scan", fontSize = 10.sp) }
            Button(
                onClick = { presenter.saveCodes(null, dev.value ?: "") },
                colors = ButtonDefaults.buttonColors(Color(0xFF597934)),
                modifier = Modifier.weight(1f)
            )
            { Text(text = "Send All", fontSize = 10.sp) }
        }
        if (presenter.isFetching) presenter.ShowSpinner()
        if (presenter.needDecision) presenter.ShowDecision()
        if (presenter.notFoundItems.isNotEmpty()) presenter.ShowNotFoundItems()
        if (presenter.needWarehouse) presenter.WarehouseDialog()
    }
}