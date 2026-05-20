package com.fs.resono.data.mediastore

import com.fs.resono.ui.model.SongUi
import java.util.concurrent.TimeUnit

fun MediaStoreSong.toSongUi(): SongUi{
    return SongUi(
        id = id,
        title = title,
        artist = artist,
        duration = formatDuration(durationMs),
        uri = contentUri,
        albumArt = albumArtUri
    )
}

private fun formatDuration(ms: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds =
        TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(minutes)
    return "%02d:%02d".format(minutes, seconds)
}