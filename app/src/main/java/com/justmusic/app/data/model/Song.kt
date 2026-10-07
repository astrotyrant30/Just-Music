package com.justmusic.app.data.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val path: String,
    val contentUri: Uri,
    val albumArtUri: Uri? = null,
    val isLiked: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L
)
