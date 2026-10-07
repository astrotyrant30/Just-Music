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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.justmusic.app.R
import com.justmusic.app.data.model.SeekBarStyle
import com.justmusic.app.data.model.Song
import com.justmusic.app.ui.components.SongListItem
import com.justmusic.app.ui.components.seekbars.CapsuleSeekBar
import com.justmusic.app.ui.components.seekbars.FluidWaveSeekBar
import com.justmusic.app.ui.components.seekbars.ModernAestheticSeekBar
import com.justmusic.app.ui.components.seekbars.SegmentedSeekBar
import com.justmusic.app.ui.components.seekbars.SeekbarSelectorDialog
import com.justmusic.app.ui.components.seekbars.WaveformSeekBar
import com.justmusic.app.ui.components.seekbars.formatPlaybackTime
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
    queue: List<Song> = emptyList(),
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleSkipSilence: () -> Unit,
    onToggleLike: (Long) -> Unit,
    onStyleSelected: (SeekBarStyle) -> Unit,
    onSelectFromQueue: (Song) -> Unit = {},
    onCollapse: () -> Unit
) {
    if (song == null) return

    // Handle Android system back press to collapse player cleanly
    BackHandler(enabled = true) {
        onCollapse()
    }

    val context = LocalContext.current
    var topColor by remember { mutableStateOf(Color(0xFF384464)) } // Dusty twilight navy (like Winter screenshot)
    var bottomColor by remember { mutableStateOf(Color(0xFF232D44)) }
    var accentButtonColor by remember { mutableStateOf(Color(0xFFFF5277)) } // Coral pink button (like Winter screenshot)
    var showSeekbarDialog by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var localSeekStyle by remember(seekBarStyle) { mutableStateOf(seekBarStyle) }

    LaunchedEffect(song.albumArtUri, song.title) {
        val extracted = GradientExtractor.extractDynamicPalette(
            context = context,
            albumArtUri = song.albumArtUri,
            fallbackSeed = "${song.title}_${song.artist}"
        )
        if (extracted != null) {
            topColor = extracted.gradientTop
            bottomColor = extracted.gradientBottom
            accentButtonColor = if (extracted.primary != Color.Transparent) extracted.primary else Color(0xFFFF5277)
        }
    }

    // 100% OPAQUE background gradient so underlying screens are NEVER visible!
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            topColor.copy(alpha = 1f),
            bottomColor.copy(alpha = 1f),
            Color(0xFF131724)
        )
    )

    // Outer solid background container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F121D)) // Solid opaque base prevents any transparency leak
            .background(bgBrush)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp, vertical = 14.dp)
        ) {
            val availableHeight = maxHeight
            val availableWidth = maxWidth

            // Responsive sizing to look majestic on both small and large displays
            val artworkSize = min(availableHeight * 0.42f, availableWidth * 0.88f)
            val verticalSpacing = if (availableHeight < 680.dp) 8.dp else 16.dp

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. TOP BAR: Centered Album Title with down caret + options menu on right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Empty box for balanced symmetry
                    Box(modifier = Modifier.size(38.dp))

                    // Center: Album title and Down Caret matching Screenshot
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(onClick = onCollapse)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = song.album.ifEmpty { "Chillhop Essentials" }.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.6.sp,
                            color = Color(0xB3FFFFFF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse",
                            tint = Color(0xCCFFFFFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Options Menu Button (...)
                    IconButton(
                        onClick = { showSeekbarDialog = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Options",
                            tint = Color(0xCCFFFFFF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 2. CHILLHOP / ALBUM STYLIZED DISPLAY BANNER (Matching Screenshot header)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "CHILLHOP",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        color = Color(0xE6FFFFFF)
                    )
                    Text(
                        text = "e s s e n t i a l s",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0x99FFFFFF)
                    )
                }

                // 3. HERO ALBUM ARTWORK CARD (Matching Screenshot rounded illustration)
                Box(
                    modifier = Modifier
                        .size(artworkSize)
                        .shadow(28.dp, RoundedCornerShape(26.dp), spotColor = Color(0x77000000))
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0x22FFFFFF))
                ) {
                    val heroImageRequest = remember(song.albumArtUri) {
                        ImageRequest.Builder(context)
                            .data(song.albumArtUri)
                            .crossfade(true)
                            .size(700, 700)
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

                Spacer(modifier = Modifier.height(verticalSpacing / 2))

                // 4. SONG TITLE & ARTIST (Matching Screenshot typography: "Snowstalgia" / "invention_")
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = song.title,
                        fontSize = if (availableHeight < 680.dp) 22.sp else 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        fontSize = if (availableHeight < 680.dp) 13.sp else 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xB3FFFFFF),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(verticalSpacing / 3))

                // 5. WAVEFORM SOUNDWAVE AUDIO SEEKBAR WITH TIMESTAMPS (Matching Screenshot exactly!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left timestamp: elapsed time e.g. "0.47" (clickable to change visualizer)
                    Text(
                        text = formatPlaybackTime(currentPositionMs),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xB3FFFFFF),
                        modifier = Modifier
                            .width(42.dp)
                            .clickable { showSeekbarDialog = true }
                    )

                    // Waveform Seeker (center) - dynamically updates in real-time when style changes
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        when (localSeekStyle) {
                            SeekBarStyle.CAPSULE -> CapsuleSeekBar(
                                currentPositionMs = currentPositionMs,
                                durationMs = durationMs,
                                onSeek = onSeekTo,
                                activeColor = Color.White
                            )
                            SeekBarStyle.FLUID_WAVE -> FluidWaveSeekBar(
                                currentPositionMs = currentPositionMs,
                                durationMs = durationMs,
                                onSeek = onSeekTo,
                                activeColor = Color.White
                            )
                            SeekBarStyle.SEGMENTED -> SegmentedSeekBar(
                                currentPositionMs = currentPositionMs,
                                durationMs = durationMs,
                                onSeek = onSeekTo,
                                activeColor = Color.White
                            )
                            else -> WaveformSeekBar(
                                currentPositionMs = currentPositionMs,
                                durationMs = durationMs,
                                onSeek = onSeekTo,
                                activeColor = Color.White,
                                inactiveColor = Color(0x44FFFFFF)
                            )
                        }
                    }

                    // Right timestamp: remaining time e.g. "-1.48" (clickable to change visualizer)
                    val remainingMs = (durationMs - currentPositionMs).coerceAtLeast(0L)
                    Text(
                        text = "-${formatPlaybackTime(remainingMs)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xB3FFFFFF),
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .width(42.dp)
                            .clickable { showSeekbarDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(verticalSpacing / 3))

                // 6. PLAYBACK CONTROLS: 5 buttons matching Screenshot!
                // Repeat, Previous, Big Prominent Circular Play/Pause, Next, Shuffle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Repeat Button
                    IconButton(
                        onClick = onToggleRepeat,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (repeatMode == 2) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = "Repeat",
                                tint = if (repeatMode != 0) PinkAccent else Color(0xCCFFFFFF),
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

                    // 2. Previous Track
                    IconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // 3. CENTER PROMINENT FLOATING CIRCULAR PLAY/PAUSE BUTTON
                    // (Matching Screenshot: vibrant coral-pink or cyan round button!)
                    Box(
                        modifier = Modifier
                            .size(if (availableHeight < 680.dp) 64.dp else 72.dp)
                            .shadow(20.dp, CircleShape, spotColor = accentButtonColor.copy(alpha = 0.6f))
                            .clip(CircleShape)
                            .background(accentButtonColor)
                            .clickable(onClick = onTogglePlayPause),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // 4. Next Track
                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // 5. Shuffle Button
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (shuffleMode) PinkAccent else Color(0xCCFFFFFF),
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
                }

                Spacer(modifier = Modifier.height(verticalSpacing / 2))

                // 7. BOTTOM STATUS BAR: Audio device output on left, Queue icon on right (Matching Screenshot)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Audio output device indicator (e.g. bluetooth / "Matt's AirPods Pro" / "Phone Speaker")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onToggleLike(song.id) }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) PinkAccent else Color(0x99FFFFFF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Device",
                            tint = Color(0x99FFFFFF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = audioDeviceName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xB3FFFFFF)
                        )
                    }

                    // Right: Queue / Playlist icon (Matching Screenshot bottom right icon!)
                    IconButton(
                        onClick = { showQueueSheet = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Up Next Queue",
                            tint = Color(0xCCFFFFFF),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Seek Bar Style Selection Dialog (ModalBottomSheet with instant live preview & dynamic update)
        if (showSeekbarDialog) {
            SeekbarSelectorDialog(
                currentStyle = localSeekStyle,
                onStyleSelected = { style: SeekBarStyle ->
                    localSeekStyle = style
                    onStyleSelected(style)
                },
                onDismiss = { showSeekbarDialog = false }
            )
        }

        // Up Next Queue Bottom Sheet
        if (showQueueSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                containerColor = Color(0xFF141926),
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Up Next",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${queue.size} tracks in queue",
                                fontSize = 12.sp,
                                color = Color(0x99FFFFFF)
                            )
                        }

                        IconButton(onClick = { showQueueSheet = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (queue.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No other songs in queue", color = Color(0x99FFFFFF))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(queue, key = { it.id }) { qSong ->
                                val isCurrent = qSong.id == song.id
                                SongListItem(
                                    song = qSong,
                                    isPlaying = isCurrent && isPlaying,
                                    isLiked = false,
                                    onClick = {
                                        onSelectFromQueue(qSong)
                                        showQueueSheet = false
                                    },
                                    onLikeToggle = { onToggleLike(qSong.id) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
