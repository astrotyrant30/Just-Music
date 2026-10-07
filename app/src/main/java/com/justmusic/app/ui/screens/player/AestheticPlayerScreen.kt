package com.justmusic.app.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.justmusic.app.R
import com.justmusic.app.data.model.SeekBarStyle
import com.justmusic.app.data.model.Song
import com.justmusic.app.ui.components.AudioOutputBadge
import com.justmusic.app.ui.components.CircularPlayButton
import com.justmusic.app.ui.components.seekbars.CapsuleSeekBar
import com.justmusic.app.ui.components.seekbars.FluidWaveSeekBar
import com.justmusic.app.ui.components.seekbars.SegmentedSeekBar
import com.justmusic.app.ui.components.seekbars.SeekbarSelectorDialog
import com.justmusic.app.ui.components.seekbars.WaveformSeekBar
import com.justmusic.app.ui.theme.GradientExtractor
import com.justmusic.app.ui.theme.PinkAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AestheticPlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isLiked: Boolean,
    shuffleMode: Boolean,
    repeatMode: Int,
    skipSilenceEnabled: Boolean,
    seekBarStyle: SeekBarStyle,
    audioDeviceName: String,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleSkipSilence: () -> Unit,
    onToggleLike: (Long) -> Unit,
    onStyleSelected: (SeekBarStyle) -> Unit,
    onCollapse: () -> Unit
) {
    if (song == null) return

    val context = LocalContext.current
    var topColor by remember { mutableStateOf(Color(0xFF282142)) }
    var bottomColor by remember { mutableStateOf(Color(0xFF131024)) }
    var showSeekbarDialog by remember { mutableStateOf(false) }

    LaunchedEffect(song.albumArtUri) {
        val colors = GradientExtractor.extractGradientColors(context, song.albumArtUri)
        topColor = colors.first
        bottomColor = colors.second
    }

    val bgBrush = Brush.verticalGradient(
        colors = listOf(topColor, bottomColor)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = song.album.ifEmpty { "Chillhop Essentials" },
                        fontFamily = FontFamily.Serif,
                        fontSize = 14.sp,
                        color = Color(0xD9FFFFFF),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { showSeekbarDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hero Artwork Card
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .aspectRatio(1f)
                    .shadow(16.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
            ) {
                AsyncImage(
                    model = song.albumArtUri,
                    placeholder = painterResource(id = R.drawable.ic_default_album_art),
                    error = painterResource(id = R.drawable.ic_default_album_art),
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Track Title & Artist Name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = song.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { onToggleLike(song.id) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) PinkAccent else Color(0xB3FFFFFF)
                        )
                    }
                }

                Text(
                    text = song.artist,
                    fontSize = 15.sp,
                    color = Color(0xB3FFFFFF),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Seek Bar Row (Time elapsed | Seek Bar | Remaining)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(currentPositionMs),
                    fontSize = 12.sp,
                    color = Color(0xB3FFFFFF)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(modifier = Modifier.weight(1f)) {
                    when (seekBarStyle) {
                        SeekBarStyle.WAVEFORM -> WaveformSeekBar(
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            onSeek = onSeekTo
                        )
                        SeekBarStyle.CAPSULE -> CapsuleSeekBar(
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            onSeek = onSeekTo
                        )
                        SeekBarStyle.SEGMENTED -> SegmentedSeekBar(
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            onSeek = onSeekTo
                        )
                        SeekBarStyle.FLUID_WAVE -> FluidWaveSeekBar(
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            onSeek = onSeekTo
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "-${formatTime(durationMs - currentPositionMs)}",
                    fontSize = 12.sp,
                    color = Color(0xB3FFFFFF)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Playback Action Control Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleRepeat) {
                    Icon(
                        imageVector = if (repeatMode == 2) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != 0) PinkAccent else Color(0x77FFFFFF)
                    )
                }

                IconButton(onClick = { onSeekTo((currentPositionMs - 10000).coerceAtLeast(0L)) }) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10s",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onSkipPrevious) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                CircularPlayButton(
                    isPlaying = isPlaying,
                    onClick = onTogglePlayPause,
                    size = 64.dp
                )

                IconButton(onClick = onSkipNext) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = { onSeekTo((currentPositionMs + 10000).coerceAtMost(durationMs)) }) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Forward 10s",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffleMode) PinkAccent else Color(0x77FFFFFF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AudioOutputBadge(deviceName = audioDeviceName)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Skip Silence indicator badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (skipSilenceEnabled) Color(0x3300E5FF) else Color(0x22FFFFFF))
                            .clickable(onClick = onToggleSkipSilence)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (skipSilenceEnabled) "Silence Skipped" else "Skip Silence Off",
                            fontSize = 11.sp,
                            color = if (skipSilenceEnabled) Color(0xFF00E5FF) else Color(0x88FFFFFF)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = { showSeekbarDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Queue / Settings",
                            tint = Color(0xB3FFFFFF)
                        )
                    }
                }
            }
        }

        // Seek Bar Style Selection Dialog
        if (showSeekbarDialog) {
            SeekbarSelectorDialog(
                currentStyle = seekBarStyle,
                onStyleSelected = onStyleSelected,
                onDismiss = { showSeekbarDialog = false }
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d.%02d", minutes, seconds)
}
