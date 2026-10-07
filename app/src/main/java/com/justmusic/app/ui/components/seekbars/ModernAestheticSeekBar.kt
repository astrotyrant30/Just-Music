package com.justmusic.app.ui.components.seekbars

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModernAestheticSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color(0x40FFFFFF),
    showTimeLabels: Boolean = true
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val actualFraction = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val displayFraction = if (isDragging) dragFraction else actualFraction
    val displayedMs = if (isDragging) (dragFraction * durationMs).toLong() else currentPositionMs

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
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
            val width = size.width
            val height = size.height
            val trackHeight = 6.dp.toPx()
            val y = (height - trackHeight) / 2f

            // Inactive Background Track
            drawRoundRect(
                color = inactiveColor,
                topLeft = Offset(0f, y),
                size = Size(width, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
            )

            // Active Track
            val activeWidth = width * displayFraction
            if (activeWidth > 0f) {
                drawRoundRect(
                    color = activeColor,
                    topLeft = Offset(0f, y),
                    size = Size(activeWidth, trackHeight),
                    cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
                )
            }

            // Scrub Thumb
            val thumbRadius = if (isDragging) 9.dp.toPx() else 7.dp.toPx()
            val thumbX = activeWidth.coerceIn(thumbRadius, width - thumbRadius)
            drawCircle(
                color = activeColor,
                radius = thumbRadius,
                center = Offset(thumbX, height / 2f)
            )

            // Outer subtle glow when dragging
            if (isDragging) {
                drawCircle(
                    color = activeColor.copy(alpha = 0.25f),
                    radius = thumbRadius + 6.dp.toPx(),
                    center = Offset(thumbX, height / 2f)
                )
            }
        }

        if (showTimeLabels) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatPlaybackTime(displayedMs),
                    fontSize = 12.sp,
                    color = Color(0xD9FFFFFF)
                )

                Text(
                    text = formatPlaybackTime(durationMs),
                    fontSize = 12.sp,
                    color = Color(0xD9FFFFFF)
                )
            }
        }
    }
}

fun formatPlaybackTime(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L) / 1000)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
