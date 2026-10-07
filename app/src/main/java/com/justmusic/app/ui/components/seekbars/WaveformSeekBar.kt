package com.justmusic.app.ui.components.seekbars

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun WaveformSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color(0x55FFFFFF)
) {
    val totalBars = 35
    val barAmplitudes = remember {
        List(totalBars) { index ->
            (0.3f + 0.7f * (sin(index * 0.4).toFloat() * 0.5f + 0.5f))
        }
    }

    val progressFraction = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(durationMs) {
                detectTapGestures { offset ->
                    if (durationMs > 0) {
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((fraction * durationMs).toLong())
                    }
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = 6.dp.toPx()
        val gap = (canvasWidth - (totalBars * barWidth)) / (totalBars - 1).coerceAtLeast(1)

        val activeBarIndex = (progressFraction * totalBars).toInt()

        for (i in 0 until totalBars) {
            val barHeight = (canvasHeight * barAmplitudes[i]).coerceAtLeast(8.dp.toPx())
            val x = i * (barWidth + gap)
            val y = (canvasHeight - barHeight) / 2f

            val isPlayed = i <= activeBarIndex
            val color = if (isPlayed) activeColor else inactiveColor

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
