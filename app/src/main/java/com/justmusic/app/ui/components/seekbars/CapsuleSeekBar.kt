package com.justmusic.app.ui.components.seekbars

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun CapsuleSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color(0x55FFFFFF)
) {
    val progressFraction = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(durationMs) {
                detectTapGestures { offset ->
                    if (durationMs > 0) {
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((fraction * durationMs).toLong())
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val barHeight = 8.dp.toPx()
        val y = (height - barHeight) / 2f

        // Inactive track
        drawRoundRect(
            color = inactiveColor,
            topLeft = Offset(0f, y),
            size = Size(width, barHeight),
            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
        )

        // Active track
        val activeWidth = width * progressFraction
        if (activeWidth > 0) {
            drawRoundRect(
                color = activeColor,
                topLeft = Offset(0f, y),
                size = Size(activeWidth, barHeight),
                cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
            )
        }

        // Thumb glow
        val thumbRadius = 10.dp.toPx()
        val thumbX = activeWidth.coerceIn(thumbRadius, width - thumbRadius)
        drawCircle(
            color = activeColor,
            radius = thumbRadius,
            center = Offset(thumbX, height / 2f)
        )
    }
}
