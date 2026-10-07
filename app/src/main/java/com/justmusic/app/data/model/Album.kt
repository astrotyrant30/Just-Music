package com.justmusic.app.data.model

import android.net.Uri

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int,
    val albumArtUri: Uri? = null
)
