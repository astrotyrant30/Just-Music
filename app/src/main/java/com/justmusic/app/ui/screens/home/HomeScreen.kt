package com.justmusic.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justmusic.app.data.model.Song
import com.justmusic.app.ui.components.SongListItem
import com.justmusic.app.ui.theme.LocalAppThemeColors
import com.justmusic.app.ui.theme.PinkAccent

import java.util.Calendar

@Composable
fun HomeScreen(
    songs: List<Song>,
    recentlyPlayed: List<Song>,
    likedSongs: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    likedSongIds: Set<Long>,
    userName: String = "Music Lover",
    sleepTimerRemainingMinutes: Int? = null,
    onSongSelect: (Song, List<Song>) -> Unit,
    onLikeToggle: (Long) -> Unit,
    onTogglePlayPause: () -> Unit = {},
    onSetSleepTimer: (Int) -> Unit = {},
    onRequestScan: () -> Unit = {}
) {
    val theme = LocalAppThemeColors.current
    val (greetingSalutation, greetingTitle) = remember(userName) { getGreetingPair(userName) }
    var selectedFilterCategory by remember { mutableStateOf("Trending") }
    val filterCategories = listOf("Trending", "Popular", "Top Chart", "Recently Added", "Favorites")

    var showSleepTimerDialog by remember { mutableStateOf(false) }

    // Find the top track: use most recently played, or first track from library
    val topSong = recentlyPlayed.firstOrNull() ?: songs.firstOrNull()
    val isTopSongPlaying = isPlaying && topSong != null && currentPlayingSong?.id == topSong.id
    val isTopSongLiked = topSong != null && likedSongIds.contains(topSong.id)

    // Actually filter and sort the songs list based on user's selection
    val displayedSongs = remember(selectedFilterCategory, songs, recentlyPlayed, likedSongs, likedSongIds) {
        when (selectedFilterCategory) {
            "Favorites" -> {
                songs.filter { likedSongIds.contains(it.id) }
            }
            "Recently Added" -> {
                // Newest songs first by id descending
                songs.sortedByDescending { it.id }
            }
            "Top Chart" -> {
                // Prioritize recently played, then longer tracks
                val recentIds = recentlyPlayed.map { it.id }.toSet()
                songs.sortedWith(compareByDescending<Song> { recentIds.contains(it.id) }.thenByDescending { it.durationMs })
            }
            "Popular" -> {
                // Show recently played tracks first, then the rest
                if (recentlyPlayed.isNotEmpty()) {
                    val remaining = songs.filter { s -> recentlyPlayed.none { it.id == s.id } }
                    recentlyPlayed + remaining
                } else {
                    songs
                }
            }
            "Trending" -> {
                // Default trending view
                if (recentlyPlayed.isNotEmpty()) recentlyPlayed else songs
            }
            else -> songs
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // 1. Top Header Bar matching Screenshot 3 left phone ("Hello, / Gustavo!")
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greetingSalutation,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = theme.textSecondary
                    )
                    Text(
                        text = greetingTitle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                }

                // Top right 4-dot menu / grid icon button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (theme.isDark) Color(0x22FFFFFF) else Color(0x0C000000))
                        .clickable { onRequestScan() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Menu / Scan",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 2. Horizontal Filter Pill Chips (Matching Screenshot 3)
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filterCategories) { category ->
                    val isSelected = category == selectedFilterCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) theme.primary
                                else theme.cardSubtle
                            )
                            .clickable { selectedFilterCategory = category }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else theme.textSecondary
                        )
                    }
                }
            }
        }

        // 3. Featured Hero Card / Weekly Top Track (Material 3 Expressive / Dynamic music colors)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                if (topSong != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = theme.primary.copy(alpha = 0.35f))
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        theme.heroGradientStart,
                                        theme.heroGradientEnd
                                    )
                                )
                            )
                            .clickable {
                                onSongSelect(topSong, displayedSongs.ifEmpty { listOf(topSong) })
                            }
                            .padding(20.dp)
                    ) {
                        Column {
                            // Top Row: Heart / Like Button on the left
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x33FFFFFF))
                                        .clickable { onLikeToggle(topSong.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isTopSongLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isTopSongLiked) PinkAccent else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                if (sleepTimerRemainingMinutes != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0x3300E5FF))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Sleep: ${sleepTimerRemainingMinutes}m",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E5FF)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Bottom Content: Title & Artist on left, Songs count on right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = topSong.title,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "By. ${topSong.artist}",
                                        fontSize = 13.sp,
                                        color = Color(0xCCFFFFFF),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "${songs.size} Songs",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xEEFFFFFF)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Buttons Row (All working and responsive)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Play / Pause Button
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable {
                                            if (currentPlayingSong?.id == topSong.id) {
                                                onTogglePlayPause()
                                            } else {
                                                onSongSelect(topSong, displayedSongs.ifEmpty { listOf(topSong) })
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isTopSongPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color(0xFF281F4B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // 2. Heart Toggle
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable { onLikeToggle(topSong.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isTopSongLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isTopSongLiked) PinkAccent else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // 3. Sleep Timer / Power Button
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable { showSleepTimerDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "Sleep Timer",
                                        tint = if (sleepTimerRemainingMinutes != null) Color(0xFF00E5FF) else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // 4. More Options
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            onSongSelect(topSong, displayedSongs)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreHoriz,
                                        contentDescription = "More",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Empty state card when no songs scanned yet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(theme.heroGradientStart, theme.heroGradientEnd)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Welcome to Just Music",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Scan your local audio files to start enjoying music",
                                fontSize = 12.sp,
                                color = Color(0xCCFFFFFF)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .clickable { onRequestScan() }
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Scan",
                                        tint = theme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Scan Music Library",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3-dot carousel indicator below card
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(theme.primary)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (theme.isDark) Color(0x44FFFFFF) else Color(0x33000000))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (theme.isDark) Color(0x44FFFFFF) else Color(0x33000000))
                    )
                }
            }
        }

        // 4. Section Title matching Screenshot 3 ("Recently Played" / Active filter)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedFilterCategory == "Trending") "Recently Played" else selectedFilterCategory,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
                Text(
                    text = "${displayedSongs.size} tracks",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.textSecondary
                )
            }
        }

        // 5. Track list items
        if (displayedSongs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (selectedFilterCategory == "Favorites") "No liked tracks yet" else "No songs found in this category",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.textSecondary
                        )
                        if (selectedFilterCategory == "Favorites") {
                            Text(
                                text = "Tap the heart on any song to add it to your favorites!",
                                fontSize = 12.sp,
                                color = theme.textSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            items(displayedSongs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isPlaying = isPlaying && currentPlayingSong?.id == song.id,
                    isLiked = likedSongIds.contains(song.id),
                    onClick = { onSongSelect(song, displayedSongs) },
                    onLikeToggle = { onLikeToggle(song.id) }
                )
            }
        }
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = {
                Text(
                    text = "Sleep Timer",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = if (sleepTimerRemainingMinutes != null)
                            "Active Timer: $sleepTimerRemainingMinutes minutes remaining"
                        else
                            "Turn off playback automatically after:",
                        fontSize = 13.sp,
                        color = Color(0xCCFFFFFF),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    listOf(15, 30, 45, 60).forEach { mins ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSetSleepTimer(mins)
                                    showSleepTimerDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = "$mins minutes",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }

                    if (sleepTimerRemainingMinutes != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSetSleepTimer(0)
                                    showSleepTimerDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = "Cancel Timer",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PinkAccent
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("Close", color = Color.White)
                }
            },
            containerColor = theme.cardBg
        )
    }
}

private fun getGreetingPair(userName: String): Pair<String, String> {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val salutation = when (hour) {
        in 5..11 -> "Good Morning,"
        in 12..16 -> "Good Afternoon,"
        in 17..21 -> "Good Evening,"
        else -> "Late Night Vibes,"
    }
    val title = if (userName.isNotBlank() && userName != "Music Lover") {
        "$userName!"
    } else {
        when (hour) {
            in 5..11 -> "Ready to Listen?"
            in 12..16 -> "Feel the Groove"
            in 17..21 -> "Time to Unwind"
            else -> "Chill & Relax"
        }
    }
    return salutation to title
}
