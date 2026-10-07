package com.justmusic.app.data.repository

import com.justmusic.app.data.db.LikedSongEntity
import com.justmusic.app.data.db.PlaybackHistoryEntity
import com.justmusic.app.data.db.PlaylistDao
import com.justmusic.app.data.db.PlaylistEntity
import com.justmusic.app.data.db.PlaylistSongCrossRef
import com.justmusic.app.data.db.SongDao
import com.justmusic.app.data.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepository(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) {

    val likedSongIds: Flow<List<Long>> = songDao.getLikedSongIds()

    val recentHistory: Flow<List<PlaybackHistoryEntity>> = songDao.getRecentHistory()

    val playlists: Flow<List<Playlist>> = playlistDao.getAllPlaylists().map { entities ->
        entities.map { entity ->
            Playlist(
                id = entity.id,
                name = entity.name,
                createdAt = entity.createdAt,
                coverArtUri = entity.coverArtUri
            )
        }
    }

    suspend fun toggleLike(songId: Long, isLikedCurrently: Boolean) {
        if (isLikedCurrently) {
            songDao.unlikeSong(songId)
        } else {
            songDao.likeSong(LikedSongEntity(songId = songId))
        }
    }

    suspend fun recordPlayback(songId: Long) {
        val existing = songDao.getHistoryForSong(songId)
        val playCount = (existing?.playCount ?: 0) + 1
        songDao.recordPlayback(
            PlaybackHistoryEntity(
                songId = songId,
                lastPlayedTimestamp = System.currentTimeMillis(),
                playCount = playCount
            )
        )
    }

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.createPlaylist(PlaylistEntity(name = name))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, songId = songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Long>> {
        return playlistDao.getSongsForPlaylist(playlistId)
    }
}
