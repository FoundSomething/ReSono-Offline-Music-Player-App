package com.fs.resono.ui.screens.player

data class PlayerUiState(
    val title: String = "",
    val artist: String = "",
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val hasActiveSong: Boolean = false
)
