package com.justmusic.app.ui.components.seekbars

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun FluidWaveSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color(0x55FFFFFF)
) {
    val transition = rememberInfiniteTransition(label = "WaveAnim")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    val progressFraction = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
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
        val centerY = height / 2f
        val activeWidth = width * progressFraction

        // Base background line
        drawLine(
            color = inactiveColor,
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 3.dp.toPx()
        )

        // Wave path for active part
        if (activeWidth > 0) {
            val wavePath = Path()
            val amplitude = 6.dp.toPx()
            val frequency = 0.05f

            wavePath.moveTo(0f, centerY)
            var x = 0f
            while (x <= activeWidth) {
                val y = centerY + sin(x * frequency + phase) * amplitude
                wavePath.lineTo(x, y)
                x += 4f
            }

            drawPath(
                path = wavePath,
                color = activeColor,
                style = Stroke(width = 4.dp.toPx())
            )

            // Playhead thumb
            drawCircle(
                color = activeColor,
                radius = 8.dp.toPx(),
                center = Offset(activeWidth, centerY)
            )
        }
    }
}
