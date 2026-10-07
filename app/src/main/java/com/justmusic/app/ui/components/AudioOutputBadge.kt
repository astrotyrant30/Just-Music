package com.justmusic.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AudioOutputBadge(
    deviceName: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xB3FFFFFF)
) {
    val isBluetooth = deviceName.contains("AirPods", ignoreCase = true) ||
            deviceName.contains("Bluetooth", ignoreCase = true) ||
            deviceName.contains("Buds", ignoreCase = true) ||
            deviceName.contains("Headset", ignoreCase = true)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isBluetooth) Icons.Default.Bluetooth else Icons.Default.Speaker,
            contentDescription = "Audio Device",
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = deviceName,
            fontSize = 12.sp,
            color = textColor
        )
    }
}
