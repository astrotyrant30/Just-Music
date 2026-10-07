package com.justmusic.app.ui.components.seekbars

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justmusic.app.data.model.SeekBarStyle
import com.justmusic.app.ui.theme.PinkAccent

@Composable
fun SeekbarSelectorDialog(
    currentStyle: SeekBarStyle,
    onStyleSelected: (SeekBarStyle) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xFF231D38))
            .padding(24.dp)
    ) {
        Text(
            text = "Select Seek Bar Style",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        SeekBarStyle.entries.forEach { style ->
            val isSelected = style == currentStyle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0x33FF5277) else Color.Transparent)
                    .clickable {
                        onStyleSelected(style)
                        onDismiss()
                    }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = {
                        onStyleSelected(style)
                        onDismiss()
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = PinkAccent)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = style.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = style.description,
                        fontSize = 12.sp,
                        color = Color(0xB3FFFFFF)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
