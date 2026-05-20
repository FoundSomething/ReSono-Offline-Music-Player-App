package com.fs.resono.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.fs.resono.audio.DspAudioProcessor
import com.fs.resono.audio.DspRenderersFactory
import com.fs.resono.ui.screens.player.PlayerUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@UnstableApi
class PlayerViewModel(
    application: Application,
    private val dspViewModel: DspViewModel
) : AndroidViewModel(application) {

    private val appContext = application.applicationContext

    // Initialize Processor
    private val dspProcessor = DspAudioProcessor(
        onReady = {
            // Called when nativeInit is done in C++
            viewModelScope.launch {
                Log.d("PlayerViewModel", "DspAudioProcessor.onReady callback triggered!") // Diagnostic log
                dspViewModel.onDspReady()
            }
        }
    )

    private val renderersFactory = DspRenderersFactory(
        appContext,
        audioProcessors = arrayOf(dspProcessor)
    )

    // Build Player with proper Audio Attributes for Focus Handling
    val player = ExoPlayer.Builder(appContext)
        .setRenderersFactory(renderersFactory)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true // true enables automatic audio focus management
        )
        .build()

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    _uiState.update { it.copy(progress = 0f) }
                    viewModelScope.launch {
                        if (!dspViewModel.isDspReady()) { // Check if it's already true from DspAudioProcessor.onReady
                            Log.d("PlayerViewModel", "Player.STATE_READY: Forcing dspViewModel.onDspReady() for debugging.")
                            dspViewModel.onDspReady()
                        } else {
                            Log.d("PlayerViewModel", "Player.STATE_READY: dspReady was already true from DspAudioProcessor.onReady.")
                        }
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Clear biquad filter memory on item changes to prevent "tail noise"
                if (mediaItem != null) {
                    viewModelScope.launch {
                        dspViewModel.flush()
                    }
                }

                _uiState.update {
                    it.copy(
                        title = mediaItem?.mediaMetadata?.title?.toString().orEmpty(),
                        artist = mediaItem?.mediaMetadata?.artist?.toString().orEmpty(),
                        hasActiveSong = mediaItem != null,
                        progress = 0f
                    )
                }
            }
        })
        observeProgress()
    }


    private fun observeProgress() {
        viewModelScope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    val duration = player.duration
                    val currentPos = player.currentPosition

                    // Safety check: duration can be C.TIME_UNSET
                    val progress = if (duration != C.TIME_UNSET && duration > 0) {
                        currentPos.toFloat() / duration.toFloat()
                    } else 0f

                    _uiState.update {
                        it.copy(progress = progress.coerceIn(0f, 1f))
                    }
                }
                delay(500)
            }
        }
    }

    fun playPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun next() = player.seekToNextMediaItem()
    fun previous() = player.seekToPreviousMediaItem()

    fun setMediaItems(items: List<MediaItem>, startIndex: Int = 0) {
        player.setMediaItems(items, startIndex, 0L)
        player.prepare()
        player.play()
    }

    fun buildMediaItem(uri: Uri, title: String, artist: String): MediaItem =
        MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .build()
            )
            .build()

    override fun onCleared() {
        Log.d("PlayerViewModel", "PlayerViewModel.onCleared() is being called!")
        player.stop()
        player.release()

        super.onCleared()
    }
} 