package com.justmusic.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    // Liked Songs
    @Query("SELECT songId FROM liked_songs ORDER BY timestamp DESC")
    fun getLikedSongIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun likeSong(likedSong: LikedSongEntity)

    @Query("DELETE FROM liked_songs WHERE songId = :songId")
    suspend fun unlikeSong(songId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE songId = :songId)")
    suspend fun isSongLiked(songId: Long): Boolean

    // History
    @Query("SELECT * FROM playback_history ORDER BY lastPlayedTimestamp DESC LIMIT 50")
    fun getRecentHistory(): Flow<List<PlaybackHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordPlayback(history: PlaybackHistoryEntity)

    @Query("SELECT * FROM playback_history WHERE songId = :songId")
    suspend fun getHistoryForSong(songId: Long): PlaybackHistoryEntity?
}
