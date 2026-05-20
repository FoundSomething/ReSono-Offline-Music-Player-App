package com.fs.resono.ui.screens.library


import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.fs.resono.viewmodel.LibraryViewModel
import com.fs.resono.viewmodel.LibraryViewModelFactory
import com.fs.resono.viewmodel.PlayerViewModel

@UnstableApi
@Composable
fun LibraryRoute(
    playerViewModel: PlayerViewModel,
    onMiniPlayerClick: () -> Unit,
    showMiniPlayer: Boolean
) {
    val context = LocalContext.current
    val viewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModelFactory(context)
    )
    LibraryScreen(
        viewModel = viewModel,
        onSongClick = { clickedSong ->

            val songs = viewModel.songs.value

            if (songs.isNotEmpty()) {

                val playlist = songs.map { song ->
                    playerViewModel.buildMediaItem(
                        uri = song.uri,
                        title = song.title,
                        artist = song.artist
                    )
                }

                val startIndex = songs.indexOfFirst {
                    it.id == clickedSong.id
                }.coerceAtLeast(0)

                playerViewModel.setMediaItems(
                    items = playlist,
                    startIndex = startIndex
                )
            }
        },
        onMiniPlayerClick = onMiniPlayerClick,
        showMiniPlayer = showMiniPlayer
    )
}


/*
@Composable
fun LibraryRoute(
    onSongClick: () -> Unit,
    onMiniPlayerClick: () -> Unit
) {
    ReadAudioPermissionGate {

        val context = LocalContext.current

        // Shared PlayerViewModel (same as Player screen)
        val playerViewModel: PlayerViewModel = viewModel(
            factory = PlayerViewModelFactory(context)
        )

        val repo = remember { MediaStoreRepository(context) }

        val songs by remember {
            mutableStateOf(
                repo.getAllMusic().map { it.toSongUi() }
            )
        }

        LibraryScreen(
            songs = songs,
            onSongClick = { song ->
                startPlayback(
                    playerViewModel = playerViewModel,
                    songs = songs,
                    clickedSong = song
                )
                onSongClick()
            },
            onMiniPlayerClick = onMiniPlayerClick
        )
    }
}

/* ---------- Playback helper ---------- */
private fun startPlayback(
    playerViewModel: PlayerViewModel,
    songs: List<SongUi>,
    clickedSong: SongUi
) {
    val mediaItems = songs.map {
        MediaItem.fromUri(it.uri)
    }

    val startIndex = songs.indexOfFirst { it.id == clickedSong.id }

    playerViewModel.setMediaItems(
        items = mediaItems,
        startIndex = startIndex
    )

    playerViewModel.play()
}
*/