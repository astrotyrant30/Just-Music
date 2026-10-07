package com.justmusic.app.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.justmusic.app.data.db.AppDatabase
import com.justmusic.app.data.model.SeekBarStyle
import com.justmusic.app.data.model.Song
import com.justmusic.app.data.repository.AudioOutputRepository
import com.justmusic.app.data.repository.MediaStoreRepository
import com.justmusic.app.data.repository.PlaylistRepository
import com.justmusic.app.data.repository.SettingsRepository
import com.justmusic.app.player.MusicPlayerManager
import com.justmusic.app.ui.components.MiniPlayerBar
import com.justmusic.app.ui.screens.home.HomeScreen
import com.justmusic.app.ui.screens.landing.LandingScreen
import com.justmusic.app.ui.screens.player.AestheticPlayerScreen
import com.justmusic.app.ui.screens.playlists.PlaylistsScreen
import com.justmusic.app.ui.screens.settings.SettingsScreen
import com.justmusic.app.ui.screens.songs.SongsScreen
import com.justmusic.app.ui.theme.PinkAccent
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember { AppDatabase.getInstance(context) }
    val mediaStoreRepository = remember { MediaStoreRepository(context) }
    val playlistRepository = remember { PlaylistRepository(database.songDao(), database.playlistDao()) }
    val settingsRepository = remember { SettingsRepository(context) }
    val audioOutputRepository = remember { AudioOutputRepository(context) }

    val musicPlayerManager = remember {
        MusicPlayerManager(
            context = context,
            playlistRepository = playlistRepository,
            settingsRepository = settingsRepository,
            audioOutputRepository = audioOutputRepository
        )
    }

    var allSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }

    val currentSong by musicPlayerManager.currentSong.collectAsState()
    val isPlaying by musicPlayerManager.isPlaying.collectAsState()
    val currentPositionMs by musicPlayerManager.currentPositionMs.collectAsState()
    val durationMs by musicPlayerManager.durationMs.collectAsState()
    val shuffleMode by musicPlayerManager.shuffleMode.collectAsState()
    val repeatMode by musicPlayerManager.repeatMode.collectAsState()
    val skipSilenceEnabled by musicPlayerManager.skipSilenceEnabled.collectAsState()
    val audioDeviceName by musicPlayerManager.audioDeviceName.collectAsState()
    val likedSongIds by musicPlayerManager.likedSongIds.collectAsState()

    val seekBarStyle by settingsRepository.seekBarStyle.collectAsState(initial = SeekBarStyle.WAVEFORM)
    val playlists by playlistRepository.playlists.collectAsState(initial = emptyList())
    val historyEntities by playlistRepository.recentHistory.collectAsState(initial = emptyList())

    val recentlyPlayedSongs = remember(historyEntities, allSongs) {
        val songMap = allSongs.associateBy { it.id }
        historyEntities.mapNotNull { songMap[it.songId] }
    }

    val likedSongsList = remember(likedSongIds, allSongs) {
        allSongs.filter { likedSongIds.contains(it.id) }
    }

    fun scanAudio() {
        scope.launch {
            isScanning = true
            allSongs = mediaStoreRepository.scanAudioFiles()
            isScanning = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            scanAudio()
        }
    }

    fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            scanAudio()
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var isPlayerExpanded by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Landing.route && !isPlayerExpanded) {
                Box {
                    Column {
                        if (currentSong != null) {
                            MiniPlayerBar(
                                song = currentSong!!,
                                isPlaying = isPlaying,
                                currentPositionMs = currentPositionMs,
                                durationMs = durationMs,
                                onTogglePlayPause = { musicPlayerManager.togglePlayPause() },
                                onSkipNext = { musicPlayerManager.playNext() },
                                onClick = { isPlayerExpanded = true }
                            )
                        }

                        NavigationBar(
                            containerColor = Color(0xFF131024),
                            contentColor = Color.White
                        ) {
                            val items = listOf(
                                Screen.Home to Icons.Default.Home,
                                Screen.Songs to Icons.Default.MusicNote,
                                Screen.Playlists to Icons.Default.QueueMusic,
                                Screen.Settings to Icons.Default.Settings
                            )

                            items.forEach { (screen, icon) ->
                                val isSelected = currentRoute == screen.route
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PinkAccent,
                                        selectedTextColor = PinkAccent,
                                        unselectedIconColor = Color(0x77FFFFFF),
                                        unselectedTextColor = Color(0x77FFFFFF),
                                        indicatorColor = Color(0x33FF5277)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Landing.route
            ) {
                composable(Screen.Landing.route) {
                    LandingScreen(
                        onGetStarted = {
                            checkAndRequestPermissions()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Landing.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        songs = allSongs,
                        recentlyPlayed = recentlyPlayedSongs,
                        likedSongs = likedSongsList,
                        currentPlayingSong = currentSong,
                        isPlaying = isPlaying,
                        likedSongIds = likedSongIds,
                        onSongSelect = { song, queue ->
                            musicPlayerManager.playSong(song, queue)
                            isPlayerExpanded = true
                        },
                        onLikeToggle = { songId -> musicPlayerManager.toggleLikeSong(songId) }
                    )
                }

                composable(Screen.Songs.route) {
                    SongsScreen(
                        songs = allSongs,
                        currentPlayingSong = currentSong,
                        isPlaying = isPlaying,
                        likedSongIds = likedSongIds,
                        onSongSelect = { song, queue ->
                            musicPlayerManager.playSong(song, queue)
                            isPlayerExpanded = true
                        },
                        onLikeToggle = { songId -> musicPlayerManager.toggleLikeSong(songId) }
                    )
                }

                composable(Screen.Playlists.route) {
                    PlaylistsScreen(
                        playlists = playlists,
                        likedSongs = likedSongsList,
                        onCreatePlaylist = { name ->
                            scope.launch { playlistRepository.createPlaylist(name) }
                        },
                        onPlaylistSelect = { playlist ->
                            // Open playlist songs
                        },
                        onLikedSongsSelect = {
                            if (likedSongsList.isNotEmpty()) {
                                musicPlayerManager.playSong(likedSongsList.first(), likedSongsList)
                                isPlayerExpanded = true
                            }
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        skipSilenceEnabled = skipSilenceEnabled,
                        seekBarStyle = seekBarStyle,
                        onToggleSkipSilence = { musicPlayerManager.toggleSkipSilence() },
                        onStyleSelected = { style ->
                            scope.launch { settingsRepository.setSeekBarStyle(style) }
                        }
                    )
                }
            }

            // Expanded Full Aesthetic Player Overlay
            AnimatedVisibility(
                visible = isPlayerExpanded && currentSong != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                AestheticPlayerScreen(
                    song = currentSong,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    isLiked = likedSongIds.contains(currentSong?.id ?: -1L),
                    shuffleMode = shuffleMode,
                    repeatMode = repeatMode,
                    skipSilenceEnabled = skipSilenceEnabled,
                    seekBarStyle = seekBarStyle,
                    audioDeviceName = audioDeviceName,
                    onTogglePlayPause = { musicPlayerManager.togglePlayPause() },
                    onSkipNext = { musicPlayerManager.playNext() },
                    onSkipPrevious = { musicPlayerManager.playPrevious() },
                    onSeekTo = { pos -> musicPlayerManager.seekTo(pos) },
                    onToggleShuffle = { musicPlayerManager.toggleShuffle() },
                    onToggleRepeat = { musicPlayerManager.toggleRepeat() },
                    onToggleSkipSilence = { musicPlayerManager.toggleSkipSilence() },
                    onToggleLike = { songId -> musicPlayerManager.toggleLikeSong(songId) },
                    onStyleSelected = { style -> scope.launch { settingsRepository.setSeekBarStyle(style) } },
                    onCollapse = { isPlayerExpanded = false }
                )
            }
        }
    }
}
