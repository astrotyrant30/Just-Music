package com.justmusic.app.ui.screens.songs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justmusic.app.data.model.Song
import com.justmusic.app.ui.components.SongListItem
import com.justmusic.app.ui.theme.LocalAppThemeColors

@Composable
fun SongsScreen(
    songs: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    likedSongIds: Set<Long>,
    onSongSelect: (Song, List<Song>) -> Unit,
    onLikeToggle: (Long) -> Unit
) {
    val theme = LocalAppThemeColors.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredSongs = remember(searchQuery, songs) {
        if (searchQuery.isBlank()) songs else {
            songs.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(top = 18.dp)
    ) {
        Text(
            text = "Music Library",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search songs, artists, albums...", color = theme.textSecondary.copy(alpha = 0.7f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = theme.primary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.primary,
                unfocusedBorderColor = if (theme.isDark) Color(0x22FFFFFF) else Color(0x18000000),
                focusedContainerColor = theme.cardBg,
                unfocusedContainerColor = theme.cardBg,
                focusedTextColor = theme.textPrimary,
                unfocusedTextColor = theme.textPrimary
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "No audio tracks in library" else "No matching tracks found",
                    fontSize = 14.sp,
                    color = theme.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                items(filteredSongs, key = { it.id }) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = isPlaying && currentPlayingSong?.id == song.id,
                        isLiked = likedSongIds.contains(song.id),
                        onClick = { onSongSelect(song, filteredSongs) },
                        onLikeToggle = { onLikeToggle(song.id) }
                    )
                }
            }
        }
    }
}
