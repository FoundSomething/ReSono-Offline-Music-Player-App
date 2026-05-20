package com.fs.resono.data.mediastore

import android.net.Uri

data class MediaStoreSong(
    val id: Long,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val contentUri: Uri,
    val albumArtUri: Uri?
)
