package com.justmusic.app.ui.screens.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.justmusic.app.R
import com.justmusic.app.data.model.SeekBarStyle
import com.justmusic.app.data.model.Song
import com.justmusic.app.ui.components.AudioOutputBadge
import com.justmusic.app.ui.components.seekbars.CapsuleSeekBar
import com.justmusic.app.ui.components.seekbars.FluidWaveSeekBar
import com.justmusic.app.ui.components.seekbars.ModernAestheticSeekBar
import com.justmusic.app.ui.components.seekbars.SegmentedSeekBar
import com.justmusic.app.ui.components.seekbars.SeekbarSelectorDialog
import com.justmusic.app.ui.components.seekbars.WaveformSeekBar
import com.justmusic.app.ui.theme.GradientExtractor
import com.justmusic.app.ui.theme.PinkAccent

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

    // Handle Android system back press to collapse player cleanly
    BackHandler(enabled = true) {
        onCollapse()
    }

    val context = LocalContext.current
    var topColor by remember { mutableStateOf(Color(0xFF1E293B)) }
    var bottomColor by remember { mutableStateOf(Color(0xFF3B82F6)) }
    var showSeekbarDialog by remember { mutableStateOf(false) }

    LaunchedEffect(song.albumArtUri, song.title) {
        val extracted = GradientExtractor.extractDynamicPalette(
            context = context,
            albumArtUri = song.albumArtUri,
            fallbackSeed = "${song.title}_${song.artist}"
        )
        if (extracted != null) {
            topColor = extracted.gradientTop
            bottomColor = extracted.gradientBottom
        }
    }

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            topColor.copy(alpha = 0.95f),
            bottomColor.copy(alpha = 0.85f),
            Color(0xFF0D0F14)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            val availableHeight = maxHeight
            val availableWidth = maxWidth

            // Responsive sizing to adapt seamlessly to any phone aspect ratio
            val artworkSize = min(availableHeight * 0.40f, availableWidth * 0.85f)
            val verticalSpacing = if (availableHeight < 680.dp) 10.dp else 18.dp

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCollapse,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x2AFFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NOW PLAYING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Color(0x99FFFFFF)
                        )
                        Text(
                            text = song.album.ifEmpty { "Just Music Library" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { showSeekbarDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x2AFFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(verticalSpacing))

                // Hero Artwork Card (Rounded square with drop shadow, matching Screenshot 3)
                Box(
                    modifier = Modifier
                        .size(artworkSize)
                        .aspectRatio(1f)
                        .shadow(24.dp, RoundedCornerShape(26.dp), spotColor = Color(0x66000000))
                        .clip(RoundedCornerShape(26.dp))
                ) {
                    val heroImageRequest = remember(song.albumArtUri) {
                        ImageRequest.Builder(context)
                            .data(song.albumArtUri)
                            .size(600, 600)
                            .build()
                    }
                    AsyncImage(
                        model = heroImageRequest,
                        placeholder = painterResource(id = R.drawable.ic_default_album_art),
                        error = painterResource(id = R.drawable.ic_default_album_art),
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(verticalSpacing))

                // Song Title, Artist and Heart Icon Row (Matching Screenshot 3 middle screen)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            fontSize = if (availableHeight < 680.dp) 19.sp else 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = song.artist,
                            fontSize = if (availableHeight < 680.dp) 13.sp else 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xCCFFFFFF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Circular heart icon container matching Screenshot 3
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isLiked) PinkAccent.copy(alpha = 0.25f) else Color(0x2AFFFFFF))
                            .clickable { onToggleLike(song.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) PinkAccent else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(verticalSpacing / 2))

                // Modern Drag-Aware Seek Bar with Timestamps (Fixing jitter/seek bug)
                ModernAestheticSeekBar(
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    onSeek = onSeekTo,
                    activeColor = Color.White,
                    inactiveColor = Color(0x35FFFFFF),
                    showTimeLabels = true
                )

                Spacer(modifier = Modifier.height(verticalSpacing / 2))

                // Playback Controls Row (Shuffle, Previous, Play/Pause, Next, Repeat)
                // Exactly 5 buttons spaced evenly so Shuffle is ALWAYS visible and never pushed off screen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Shuffle Button
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (shuffleMode) PinkAccent else Color(0xB3FFFFFF),
                                modifier = Modifier.size(24.dp)
                            )
                            if (shuffleMode) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(PinkAccent)
                                )
                            }
                        }
                    }

                    // 2. Previous Button
                    IconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // 3. Main Play/Pause Button (Matching Screenshot 3 center prominent button)
                    Box(
                        modifier = Modifier
                            .size(if (availableHeight < 680.dp) 60.dp else 68.dp)
                            .shadow(16.dp, CircleShape, spotColor = Color(0x88000000))
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(onClick = onTogglePlayPause),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // 4. Next Button
                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // 5. Repeat Button
                    IconButton(
                        onClick = onToggleRepeat,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (repeatMode == 2) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = "Repeat",
                                tint = if (repeatMode != 0) PinkAccent else Color(0xB3FFFFFF),
                                modifier = Modifier.size(24.dp)
                            )
                            if (repeatMode != 0) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(PinkAccent)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(verticalSpacing / 2))

                // Footer Device & Status Bar (Matching Screenshot 3 bottom pill)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x26FFFFFF))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AudioOutputBadge(deviceName = audioDeviceName)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Skip Silence toggle badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (skipSilenceEnabled) Color(0x3300E5FF) else Color(0x1AFFFFFF))
                                    .clickable(onClick = onToggleSkipSilence)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (skipSilenceEnabled) "Skip Silence: ON" else "Skip Silence: OFF",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (skipSilenceEnabled) Color(0xFF00E5FF) else Color(0x99FFFFFF)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { showSeekbarDialog = true },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Seekbar Style",
                                    tint = Color(0xB3FFFFFF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
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
