package com.fs.resono.ui.model

import android.net.Uri

data class SongUi(
    val id: Long,
    val title: String,
    val uri: Uri,
    val artist: String,
    val duration: String,
    val albumArt: Uri?
)
