package com.biztools.stockcount.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ItemNotFoundAlertBox(
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = { onClose() }, DialogProperties(dismissOnClickOutside = false)) {
        Card {
            Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "not-found",
                        tint = Color(0xFFFF5722),
                        modifier = Modifier.size(40.dp)
                    )
                    Text(text = "Item not found")
                    Text(text = "Do you want to add new item?", fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            modifier = Modifier.weight(1f), onClick = { onConfirm() },
                            colors = ButtonDefaults.buttonColors(Color(0xFF64883A))
                        ) {
                            Text(text = "Yes")
                        }
                        Button(
                            modifier = Modifier.weight(1f), onClick = { onClose() },
                            colors = ButtonDefaults.buttonColors(Color.Gray)
                        ) {
                            Text(text = "No")
                        }
                    }
                }
            }
        }
    }
}