package com.justmusic.app.ui.screens.playlists

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justmusic.app.data.model.Playlist
import com.justmusic.app.data.model.Song
import com.justmusic.app.data.repository.PlaylistRepository
import com.justmusic.app.ui.components.AddSongsToPlaylistDialog
import com.justmusic.app.ui.components.AddToPlaylistDialog
import com.justmusic.app.ui.components.SongListItem
import com.justmusic.app.ui.theme.LocalAppThemeColors
import com.justmusic.app.ui.theme.PinkAccent
import kotlinx.coroutines.launch

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    allSongs: List<Song>,
    likedSongs: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    likedSongIds: Set<Long>,
    playlistRepository: PlaylistRepository,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onSongSelect: (Song, List<Song>) -> Unit,
    onLikeToggle: (Long) -> Unit
) {
    val theme = LocalAppThemeColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistNameInput by remember { mutableStateOf("") }

    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var showLikedSongsDetail by remember { mutableStateOf(false) }

    var songForAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var showAddSongsPicker by remember { mutableStateOf(false) }
    var showDeletePlaylistConfirm by remember { mutableStateOf(false) }

    // Intercept back button if viewing playlist detail
    BackHandler(enabled = selectedPlaylist != null || showLikedSongsDetail) {
        if (selectedPlaylist != null) selectedPlaylist = null
        if (showLikedSongsDetail) showLikedSongsDetail = false
    }

    // 1. DETAIL VIEW: LIKED SONGS
    if (showLikedSongsDetail) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.background)
                .padding(top = 16.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showLikedSongsDetail = false },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(theme.cardSubtle)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "Liked Songs",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Liked Songs Hero Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = PinkAccent.copy(alpha = 0.35f))
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        theme.heroGradientStart,
                                        theme.heroGradientEnd
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0x33FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = PinkAccent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "Liked Songs",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${likedSongs.size} tracks saved",
                                        fontSize = 13.sp,
                                        color = Color(0xCCFFFFFF)
                                    )
                                }
                            }

                            if (likedSongs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Play All Button
                                    Button(
                                        onClick = {
                                            onSongSelect(likedSongs.first(), likedSongs)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color(0xFF0F172A),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Play All",
                                            color = Color(0xFF0F172A),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Shuffle Button
                                    Button(
                                        onClick = {
                                            val shuffled = likedSongs.shuffled()
                                            onSongSelect(shuffled.first(), shuffled)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shuffle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Shuffle",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (likedSongs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No liked tracks yet.\nTap the heart icon on any song to add it here!",
                                fontSize = 14.sp,
                                color = theme.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(likedSongs, key = { it.id }) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = isPlaying && currentPlayingSong?.id == song.id,
                            isLiked = true,
                            onClick = { onSongSelect(song, likedSongs) },
                            onLikeToggle = { onLikeToggle(song.id) },
                            onOptionsClick = { songForAddToPlaylist = song }
                        )
                    }
                }
            }
        }
    }
    // 2. DETAIL VIEW: CUSTOM PLAYLIST
    else if (selectedPlaylist != null) {
        val currentPlaylist = selectedPlaylist!!
        val playlistSongIds by playlistRepository.getSongsForPlaylist(currentPlaylist.id).collectAsState(initial = emptyList())

        val playlistSongs = remember(playlistSongIds, allSongs) {
            val songMap = allSongs.associateBy { it.id }
            playlistSongIds.mapNotNull { songMap[it] }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.background)
                .padding(top = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { selectedPlaylist = null },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(theme.cardSubtle)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = currentPlaylist.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { showDeletePlaylistConfirm = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(theme.cardSubtle)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Playlist",
                        tint = Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Custom Playlist Hero Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = theme.primary.copy(alpha = 0.35f))
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        theme.heroGradientStart,
                                        theme.heroGradientEnd
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0x33FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentPlaylist.name,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${playlistSongs.size} tracks",
                                        fontSize = 13.sp,
                                        color = Color(0xCCFFFFFF)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Action buttons row: Add Songs, Play All, Shuffle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Add Songs Button
                                Button(
                                    onClick = { showAddSongsPicker = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlaylistAdd,
                                        contentDescription = null,
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+ Add Songs",
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                if (playlistSongs.isNotEmpty()) {
                                    // Play All Button
                                    Button(
                                        onClick = {
                                            onSongSelect(playlistSongs.first(), playlistSongs)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Play",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    // Shuffle Button
                                    Button(
                                        onClick = {
                                            val shuffled = playlistSongs.shuffled()
                                            onSongSelect(shuffled.first(), shuffled)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF)),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shuffle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Mix",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (playlistSongs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "This playlist is empty",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = theme.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap '+ Add Songs' above to add tracks from your library!",
                                    fontSize = 13.sp,
                                    color = theme.textSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(playlistSongs, key = { it.id }) { song ->
                        var showSongMenu by remember { mutableStateOf(false) }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            SongListItem(
                                song = song,
                                isPlaying = isPlaying && currentPlayingSong?.id == song.id,
                                isLiked = likedSongIds.contains(song.id),
                                onClick = { onSongSelect(song, playlistSongs) },
                                onLikeToggle = { onLikeToggle(song.id) },
                                onOptionsClick = { showSongMenu = true }
                            )

                            DropdownMenu(
                                expanded = showSongMenu,
                                onDismissRequest = { showSongMenu = false },
                                modifier = Modifier.background(theme.cardBg)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Remove from playlist", color = Color(0xFFEF4444)) },
                                    onClick = {
                                        showSongMenu = false
                                        scope.launch {
                                            playlistRepository.removeSongFromPlaylist(currentPlaylist.id, song.id)
                                            Toast.makeText(context, "Removed from \"${currentPlaylist.name}\"", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Add to another playlist", color = theme.textPrimary) },
                                    onClick = {
                                        showSongMenu = false
                                        songForAddToPlaylist = song
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Songs from library dialog
        if (showAddSongsPicker) {
            AddSongsToPlaylistDialog(
                playlistName = currentPlaylist.name,
                allSongs = allSongs,
                existingSongIds = playlistSongIds.toSet(),
                onDismiss = { showAddSongsPicker = false },
                onAddSongs = { songIdsToAdd ->
                    scope.launch {
                        songIdsToAdd.forEach { sid ->
                            playlistRepository.addSongToPlaylist(currentPlaylist.id, sid)
                        }
                        Toast.makeText(context, "Added ${songIdsToAdd.size} songs to \"${currentPlaylist.name}\"", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Delete playlist confirmation dialog
        if (showDeletePlaylistConfirm) {
            AlertDialog(
                onDismissRequest = { showDeletePlaylistConfirm = false },
                title = { Text("Delete Playlist", color = theme.textPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete \"${currentPlaylist.name}\"?", color = theme.textSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeletePlaylistConfirm = false
                            onDeletePlaylist(currentPlaylist.id)
                            selectedPlaylist = null
                            Toast.makeText(context, "Deleted \"${currentPlaylist.name}\"", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeletePlaylistConfirm = false }) {
                        Text("Cancel", color = theme.textSecondary)
                    }
                },
                containerColor = theme.cardBg
            )
        }
    }
    // 3. MAIN OVERVIEW VIEW
    else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.background)
                .padding(top = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Playlists",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(theme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Playlist",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Liked Songs Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = theme.primary.copy(alpha = 0.35f))
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        theme.heroGradientStart,
                                        theme.heroGradientEnd
                                    )
                                )
                            )
                            .clickable { showLikedSongsDetail = true }
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x33FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Liked Songs",
                                    tint = PinkAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Liked Songs",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${likedSongs.size} tracks saved",
                                    fontSize = 13.sp,
                                    color = Color(0xCCFFFFFF)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable {
                                        if (likedSongs.isNotEmpty()) {
                                            onSongSelect(likedSongs.first(), likedSongs)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Liked",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Custom Playlists Section Header
                item {
                    Text(
                        text = "Your Collections",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (playlists.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(theme.cardBg)
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    tint = theme.textSecondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No custom playlists yet",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = theme.textSecondary
                                )
                                Text(
                                    text = "Tap the + button to create a playlist",
                                    fontSize = 12.sp,
                                    color = theme.textSecondary.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(playlists, key = { it.id }) { playlist ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(theme.cardBg)
                                .clickable { selectedPlaylist = playlist }
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(theme.cardSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = "Playlist",
                                        tint = theme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = theme.textPrimary
                                    )
                                    Text(
                                        text = "Custom Playlist • Tap to open",
                                        fontSize = 12.sp,
                                        color = theme.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Playlist Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Playlist", color = theme.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = playlistNameInput,
                    onValueChange = { playlistNameInput = it },
                    label = { Text("Playlist Name", color = theme.textSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primary,
                        unfocusedBorderColor = theme.divider,
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistNameInput.isNotBlank()) {
                            onCreatePlaylist(playlistNameInput.trim())
                            playlistNameInput = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) {
                    Text("Create", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = theme.textSecondary)
                }
            },
            containerColor = theme.cardBg
        )
    }

    // Add To Playlist Dialog (when tapping 3 dots on a song)
    if (songForAddToPlaylist != null) {
        AddToPlaylistDialog(
            song = songForAddToPlaylist!!,
            playlists = playlists,
            onDismiss = { songForAddToPlaylist = null },
            onSelectPlaylist = { p ->
                scope.launch {
                    playlistRepository.addSongToPlaylist(p.id, songForAddToPlaylist!!.id)
                }
            },
            onCreateAndAdd = { name ->
                scope.launch {
                    val pid = playlistRepository.createPlaylist(name)
                    playlistRepository.addSongToPlaylist(pid, songForAddToPlaylist!!.id)
                }
            }
        )
    }
}
