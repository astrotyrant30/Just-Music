package com.justmusic.app.ui.components.seekbars

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val actualFraction = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val progressFraction = if (isDragging) dragFraction else actualFraction

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(durationMs) {
                detectTapGestures { offset ->
                    if (durationMs > 0 && size.width > 0) {
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((fraction * durationMs).toLong())
                    }
                }
            }
            .pointerInput(durationMs) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (durationMs > 0 && size.width > 0) {
                            isDragging = true
                            dragFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (durationMs > 0 && size.width > 0) {
                            dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    },
                    onDragEnd = {
                        if (durationMs > 0) {
                            onSeek((dragFraction * durationMs).toLong())
                        }
                        isDragging = false
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
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
