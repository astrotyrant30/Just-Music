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

    private var sleepTimerJob: Job? = null
    private val _sleepTimerRemainingMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingMinutes: StateFlow<Int?> = _sleepTimerRemainingMinutes.asStateFlow()

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _sleepTimerRemainingMinutes.value = null
            return
        }
        _sleepTimerRemainingMinutes.value = minutes
        sleepTimerJob = scope.launch {
            var remaining = minutes
            while (remaining > 0) {
                delay(60_000L)
                remaining--
                _sleepTimerRemainingMinutes.value = if (remaining > 0) remaining else null
            }
            controller?.pause()
            _sleepTimerRemainingMinutes.value = null
        }
    }

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
            } else if (playbackState == Player.STATE_ENDED) {
                if (_repeatMode.value == Player.REPEAT_MODE_ONE) {
                    seekTo(0L)
                    controller?.play()
                } else {
                    playNext()
                }
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
                if (_isPlaying.value) {
                    controller?.let { c ->
                        _currentPositionMs.value = c.currentPosition.coerceAtLeast(0L)
                        _durationMs.value = c.duration.coerceAtLeast(0L)
                    }
                    delay(250)
                } else {
                    // When paused, idle at 1s intervals to eliminate CPU waking and RAM churn
                    delay(1000)
                }
            }
        }
    }

    fun playSong(song: Song, songQueue: List<Song> = listOf(song)) {
        val actualQueue = if (songQueue.isEmpty()) listOf(song) else songQueue
        _queue.value = actualQueue
        val index = actualQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        val mediaItems = actualQueue.map { s ->
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
        val q = _queue.value
        val current = _currentSong.value
        if (q.isNotEmpty()) {
            val currentIndex = q.indexOfFirst { it.id == current?.id }
            val nextIndex = if (_shuffleMode.value) {
                if (q.size > 1) {
                    val remaining = q.indices.filter { it != currentIndex }
                    remaining.random()
                } else 0
            } else {
                if (currentIndex in 0 until (q.size - 1)) {
                    currentIndex + 1
                } else if (_repeatMode.value == Player.REPEAT_MODE_ALL || q.size > 1) {
                    0
                } else {
                    -1
                }
            }

            if (nextIndex in q.indices) {
                val nextSong = q[nextIndex]
                _currentSong.value = nextSong
                scope.launch {
                    playlistRepository.recordPlayback(nextSong.id)
                    settingsRepository.setLastPlayedSongId(nextSong.id)
                }
                val c = controller
                if (c != null && c.mediaItemCount == q.size) {
                    c.seekToDefaultPosition(nextIndex)
                    c.play()
                } else {
                    playSong(nextSong, q)
                }
                return
            }
        }
        controller?.seekToNextMediaItem()
    }

    fun playPrevious() {
        val pos = controller?.currentPosition ?: _currentPositionMs.value
        if (pos > 3000L) {
            seekTo(0L)
            return
        }
        val q = _queue.value
        val current = _currentSong.value
        if (q.isNotEmpty()) {
            val currentIndex = q.indexOfFirst { it.id == current?.id }
            val prevIndex = if (_shuffleMode.value) {
                if (q.size > 1) {
                    val remaining = q.indices.filter { it != currentIndex }
                    remaining.random()
                } else 0
            } else {
                if (currentIndex > 0) {
                    currentIndex - 1
                } else if (_repeatMode.value == Player.REPEAT_MODE_ALL || q.size > 1) {
                    q.size - 1
                } else {
                    0
                }
            }

            if (prevIndex in q.indices) {
                val prevSong = q[prevIndex]
                _currentSong.value = prevSong
                scope.launch {
                    playlistRepository.recordPlayback(prevSong.id)
                    settingsRepository.setLastPlayedSongId(prevSong.id)
                }
                val c = controller
                if (c != null && c.mediaItemCount == q.size) {
                    c.seekToDefaultPosition(prevIndex)
                    c.play()
                } else {
                    playSong(prevSong, q)
                }
                return
            }
        }
        controller?.seekToPreviousMediaItem()
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
                scope.launch {
                    playlistRepository.recordPlayback(song.id)
                    settingsRepository.setLastPlayedSongId(song.id)
                }
            }
        }
    }

    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
