package com.fs.resono.data.mediastore

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import android.net.Uri
import com.fs.resono.ui.model.SongUi

class MediaStoreRepository(
    private val context: Context
) {

    fun loadSongs(): List<SongUi> {
        val songs = mutableListOf<SongUi>()
        val resolver = context.contentResolver

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        resolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->

            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol)
                val artist = cursor.getString(artistCol)
                val durationMs = cursor.getLong(durationCol)
                val albumId = cursor.getLong(albumIdCol)

                val songUri = Uri.withAppendedPath(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )

                val albumArtUri = if (albumId > 0)
                    Uri.parse("content://media/external/audio/albumart/$albumId")
                else
                    null

                songs.add(
                    SongUi(
                    id = id,
                    title = title,
                    artist = artist,
                    duration = formatDuration(durationMs),
                    uri = songUri,
                    albumArt = albumArtUri
                    )
                )
            }
        }

        return songs
    }
    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
}