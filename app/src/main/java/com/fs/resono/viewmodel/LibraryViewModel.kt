package com.fs.resono.viewmodel

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.fs.resono.data.mediastore.MediaStoreRepository
import com.fs.resono.ui.model.SongUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LibraryViewModel(
    private val context: Context
) : ViewModel() {
    private val _songs = MutableStateFlow<List<SongUi>>(emptyList())
    val songs: StateFlow<List<SongUi>> = _songs
    init {
        loadSongs()
    }


    fun loadSongs() {
        val resolver = context.contentResolver

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC}=1"

        val result= mutableListOf<SongUi>()

        resolver.query(uri, projection, selection, null, MediaStore.Audio.Media.TITLE + " ASC")?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol)
                val artist = cursor.getString(artistCol)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                result.add(
                    SongUi(
                        id = id,
                        uri = contentUri,
                        title = title,
                        artist = artist,
                        duration = "",
                        albumArt = null
                    )
                )
            }
        }
        _songs.value = result
    }
}