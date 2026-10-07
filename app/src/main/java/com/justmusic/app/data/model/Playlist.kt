package com.justmusic.app.data.model

data class Playlist(
    val id: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val songCount: Int = 0,
    val coverArtUri: String? = null
)
