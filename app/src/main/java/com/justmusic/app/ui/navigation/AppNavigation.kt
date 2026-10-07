package com.justmusic.app.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.justmusic.app.ui.theme.DarkBackground
import com.justmusic.app.ui.theme.DynamicMusicColors
import com.justmusic.app.ui.theme.GradientExtractor
import com.justmusic.app.ui.theme.JustMusicTheme
import com.justmusic.app.ui.theme.LocalAppThemeColors
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
    val sleepTimerRemainingMinutes by musicPlayerManager.sleepTimerRemainingMinutes.collectAsState()

    val isOnboardingCompleted by settingsRepository.isOnboardingCompleted.collectAsState(initial = null)
    val currentThemeMode by settingsRepository.themeMode.collectAsState(initial = "DARK")
    val userName by settingsRepository.userName.collectAsState(initial = "Music Lover")
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

    var dynamicColors by remember { mutableStateOf<DynamicMusicColors?>(null) }

    // When music is playing, dynamically extract album art palette and theme the entire app
    LaunchedEffect(currentSong, isPlaying) {
        if (isPlaying && currentSong != null) {
            val extracted = GradientExtractor.extractDynamicPalette(
                context = context,
                albumArtUri = currentSong?.albumArtUri,
                fallbackSeed = "${currentSong?.title}_${currentSong?.artist}"
            )
            dynamicColors = extracted
        } else if (!isPlaying) {
            dynamicColors = null
        }
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

    // Automatically check permissions and scan audio if onboarding already completed
    LaunchedEffect(isOnboardingCompleted) {
        if (isOnboardingCompleted == true) {
            checkAndRequestPermissions()
        }
    }

    // Wait until initial onboarding check is loaded from DataStore
    if (isOnboardingCompleted == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        )
        return
    }

    val startDestination = if (isOnboardingCompleted == true) Screen.Home.route else Screen.Landing.route
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var isPlayerExpanded by remember { mutableStateOf(false) }

    // Intercept Back button:
    // 1. If player is open, collapse player
    // 2. If on non-home tab (Songs, Playlists, Settings), navigate back to Home instead of exiting app
    BackHandler(enabled = isPlayerExpanded) {
        isPlayerExpanded = false
    }

    BackHandler(enabled = !isPlayerExpanded && currentRoute != Screen.Home.route && currentRoute != Screen.Landing.route) {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Home.route) { inclusive = false }
            launchSingleTop = true
        }
    }

    JustMusicTheme(themeMode = currentThemeMode, dynamicColors = dynamicColors) {
        val theme = LocalAppThemeColors.current

        Scaffold(
            containerColor = theme.background,
            bottomBar = {
                if (currentRoute != Screen.Landing.route && !isPlayerExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        // Floating Mini Player Bar matching Screenshot 3 top-left widget
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

                        // Floating Modern Navigation Bar matching Screenshot 3 bottom pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 2.dp)
                                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color(0x33000000))
                                .clip(RoundedCornerShape(32.dp))
                        ) {
                            NavigationBar(
                                containerColor = theme.navBg,
                                contentColor = theme.textPrimary,
                                tonalElevation = 0.dp
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
                                            if (currentRoute != screen.route) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(Screen.Home.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = { Icon(icon, contentDescription = screen.title) },
                                        label = { Text(screen.title, fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = theme.primary,
                                            selectedTextColor = theme.primary,
                                            unselectedIconColor = theme.textSecondary,
                                            unselectedTextColor = theme.textSecondary,
                                            indicatorColor = theme.primary.copy(alpha = 0.15f)
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
                startDestination = startDestination
            ) {
                composable(Screen.Landing.route) {
                    LandingScreen(
                        onGetStarted = {
                            scope.launch {
                                settingsRepository.setOnboardingCompleted(true)
                            }
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
                        userName = userName,
                        sleepTimerRemainingMinutes = sleepTimerRemainingMinutes,
                        onSongSelect = { song, queue ->
                            musicPlayerManager.playSong(song, queue)
                            isPlayerExpanded = true
                        },
                        onLikeToggle = { songId -> musicPlayerManager.toggleLikeSong(songId) },
                        onTogglePlayPause = { musicPlayerManager.togglePlayPause() },
                        onSetSleepTimer = { mins -> musicPlayerManager.setSleepTimer(mins) },
                        onRequestScan = { checkAndRequestPermissions() }
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
                            // Play or open custom playlist
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
                        currentThemeMode = currentThemeMode,
                        skipSilenceEnabled = skipSilenceEnabled,
                        seekBarStyle = seekBarStyle,
                        userName = userName,
                        onThemeSelected = { mode ->
                            scope.launch { settingsRepository.setThemeMode(mode) }
                        },
                        onToggleSkipSilence = { musicPlayerManager.toggleSkipSilence() },
                        onStyleSelected = { style ->
                            scope.launch { settingsRepository.setSeekBarStyle(style) }
                        },
                        onUserNameChange = { newName ->
                            scope.launch { settingsRepository.setUserName(newName) }
                        }
                    )
                }
            }

            // Expanded Full Aesthetic Player Modal Overlay
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
}
