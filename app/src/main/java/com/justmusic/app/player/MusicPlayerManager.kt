package com.justmusic.app.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.justmusic.app.data.model.Song
import com.justmusic.app.data.repository.AudioOutputRepository
import com.justmusic.app.data.repository.PlaylistRepository
import com.justmusic.app.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicPlayerManager(
    private val context: Context,
    private val playlistRepository: PlaylistRepository,
    private val settingsRepository: SettingsRepository,
    private val audioOutputRepository: AudioOutputRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _skipSilenceEnabled = MutableStateFlow(true)
    val skipSilenceEnabled: StateFlow<Boolean> = _skipSilenceEnabled.asStateFlow()

    private val _audioDeviceName = MutableStateFlow("Phone Speaker")
    val audioDeviceName: StateFlow<String> = _audioDeviceName.asStateFlow()

    private val _likedSongIds = MutableStateFlow<Set<Long>>(emptySet())
    val likedSongIds: StateFlow<Set<Long>> = _likedSongIds.asStateFlow()

    init {
        initController()
        observeAudioDevice()
        observeLikedSongs()
        observeSettings()
        startPositionUpdates()
    }

    private fun initController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            controller?.addListener(playerListener)
            updateStateFromController()
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateCurrentSongFromMediaItem(mediaItem)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                _durationMs.value = controller?.duration?.coerceAtLeast(0L) ?: 0L
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _shuffleMode.value = shuffleModeEnabled
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = repeatMode
        }
    }

    private fun observeAudioDevice() {
        scope.launch {
            audioOutputRepository.currentAudioDeviceName.collectLatest { name ->
                _audioDeviceName.value = name
            }
        }
    }

    private fun observeLikedSongs() {
        scope.launch {
            playlistRepository.likedSongIds.collectLatest { ids ->
                _likedSongIds.value = ids.toSet()
            }
        }
    }

    private fun observeSettings() {
        scope.launch {
            settingsRepository.skipSilenceEnabled.collectLatest { enabled ->
                _skipSilenceEnabled.value = enabled
            }
        }
    }

    private fun startPositionUpdates() {
        scope.launch {
            while (isActive) {
                controller?.let { c ->
                    if (c.isPlaying) {
                        _currentPositionMs.value = c.currentPosition.coerceAtLeast(0L)
                        _durationMs.value = c.duration.coerceAtLeast(0L)
                    }
                }
                delay(250)
            }
        }
    }

    fun playSong(song: Song, songQueue: List<Song> = listOf(song)) {
        _queue.value = songQueue
        val index = songQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        val mediaItems = songQueue.map { s ->
            MediaItem.Builder()
                .setMediaId(s.id.toString())
                .setUri(s.contentUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artist)
                        .setAlbumTitle(s.album)
                        .setArtworkUri(s.albumArtUri)
                        .build()
                )
                .build()
        }

        controller?.run {
            setMediaItems(mediaItems, index, 0L)
            prepare()
            play()
        }

        _currentSong.value = song
        scope.launch {
            playlistRepository.recordPlayback(song.id)
            settingsRepository.setLastPlayedSongId(song.id)
        }
    }

    fun togglePlayPause() {
        controller?.let { c ->
            if (c.isPlaying) {
                c.pause()
            } else {
                c.play()
            }
        }
    }

    fun playNext() {
        controller?.seekToNext()
    }

    fun playPrevious() {
        controller?.seekToPrevious()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun toggleShuffle() {
        val next = !_shuffleMode.value
        controller?.shuffleModeEnabled = next
        _shuffleMode.value = next
    }

    fun toggleRepeat() {
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        controller?.repeatMode = nextMode
        _repeatMode.value = nextMode
    }

    fun toggleSkipSilence() {
        val next = !_skipSilenceEnabled.value
        _skipSilenceEnabled.value = next
        scope.launch {
            settingsRepository.setSkipSilenceEnabled(next)
        }
    }

    fun toggleLikeSong(songId: Long) {
        val isLiked = _likedSongIds.value.contains(songId)
        scope.launch {
            playlistRepository.toggleLike(songId, isLiked)
        }
    }

    private fun updateStateFromController() {
        controller?.let { c ->
            _isPlaying.value = c.isPlaying
            _shuffleMode.value = c.shuffleModeEnabled
            _repeatMode.value = c.repeatMode
            _durationMs.value = c.duration.coerceAtLeast(0L)
            _currentPositionMs.value = c.currentPosition.coerceAtLeast(0L)
            updateCurrentSongFromMediaItem(c.currentMediaItem)
        }
    }

    private fun updateCurrentSongFromMediaItem(mediaItem: MediaItem?) {
        val idStr = mediaItem?.mediaId
        if (idStr != null) {
            val songId = idStr.toLongOrNull()
            val song = _queue.value.find { it.id == songId }
            if (song != null) {
                _currentSong.value = song
            }
        }
    }

    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
