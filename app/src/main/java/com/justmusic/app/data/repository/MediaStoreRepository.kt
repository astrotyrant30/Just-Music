package com.justmusic.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.justmusic.app.data.model.Album
import com.justmusic.app.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreRepository(private val context: Context) {

    suspend fun scanAudioFiles(minDurationMs: Long = 30_000L): List<Song> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(minDurationMs.toString())
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use { c ->
            val count = c.count
            val songList = ArrayList<Song>(count)

            val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val pathColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            val artworkUriBase = Uri.parse("content://media/external/audio/albumart")

            while (c.moveToNext()) {
                val id = c.getLong(idColumn)
                val title = c.getString(titleColumn) ?: "Unknown Title"
                val artist = c.getString(artistColumn) ?: "Unknown Artist"
                val album = c.getString(albumColumn) ?: "Unknown Album"
                val albumId = c.getLong(albumIdColumn)
                val duration = c.getLong(durationColumn)
                val path = c.getString(pathColumn) ?: ""

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val albumArtUri = ContentUris.withAppendedId(artworkUriBase, albumId)

                songList.add(
                    Song(
                        id = id,
                        title = title,
                        artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                        album = if (album == "<unknown>") "Unknown Album" else album,
                        albumId = albumId,
                        durationMs = duration,
                        path = path,
                        contentUri = contentUri,
                        albumArtUri = albumArtUri
                    )
                )
            }
            songList
        } ?: emptyList()
    }

    suspend fun getAlbums(songs: List<Song>): List<Album> = withContext(Dispatchers.IO) {
        songs.groupBy { it.albumId }.map { (albumId, albumSongs) ->
            val firstSong = albumSongs.first()
            Album(
                id = albumId,
                title = firstSong.album,
                artist = firstSong.artist,
                songCount = albumSongs.size,
                albumArtUri = firstSong.albumArtUri
            )
        }
    }
}
