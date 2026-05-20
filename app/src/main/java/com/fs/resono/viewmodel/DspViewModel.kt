package com.fs.resono.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.fs.resono.dsp.DspEngine
import com.fs.resono.dsp.DspPreferences
import com.fs.resono.dsp.DspState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@UnstableApi
class DspViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val prefs = DspPreferences(application)

    // 1. CRITICAL: @Volatile ensures that when we set this to false in onPlayerReleased,
    // the Audio thread sees it INSTANTLY and stops calling native C++ functions.
    private @Volatile var dspReady = true
    fun isDspReady(): Boolean = dspReady
    var eqEnabled by mutableStateOf(true)
        private set
    var masterGain by mutableFloatStateOf(1.0f)
        private set
    var limiterEnabled by mutableStateOf(true)
        private set
    var limiterThreshold by mutableFloatStateOf(1.0f)
        private set

    /* ---------- EQ Bands ---------- */
    var band31 by mutableFloatStateOf(0f)
        private set
    var band62 by mutableFloatStateOf(0f)
        private set
    var band125 by mutableFloatStateOf(0f)
        private set
    var band250 by mutableFloatStateOf(0f)
        private set
    var band500 by mutableFloatStateOf(0f)
        private set
    var band1k by mutableFloatStateOf(0f)
        private set
    var band2k by mutableFloatStateOf(0f)
        private set
    var band4k by mutableFloatStateOf(0f)
        private set
    var reverbEnabled by mutableStateOf(false) // Off by default
        private set
    var reverbLevel by mutableFloatStateOf(0.5f) // 0.0f (dry) to 1.0f (wet) range expected
        private set
    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            restoreState()
        }
    }

    private suspend fun restoreState() {
        val state = prefs.dspState.first()
        eqEnabled = state.eqEnabled
        masterGain = state.masterGain

        reverbEnabled = state.reverbEnabled
        reverbLevel = state.reverbLevel

        val b = state.bands
        if (b.size >= 8) {
            band31 = b[0]; band62 = b[1]; band125 = b[2]; band250 = b[3]
            band500 = b[4]; band1k = b[5]; band2k = b[6]; band4k = b[7]
        }
        if (dspReady) {
            syncToNative()
        }
    }

    /* ---------- Updates with "Safety Gates" ---------- */

    fun updateEqEnabled(enabled: Boolean) {
        eqEnabled = enabled
        if (dspReady) DspEngine.nativeSetEqEnabled(enabled)
        debouncedSave()
    }

    fun updateMasterGain(gain: Float) {
        masterGain = gain
        if (dspReady) DspEngine.nativeSetMasterGain(gain)
        debouncedSave()
    }

    fun updateBand(index: Int, value: Float) {
        when (index) {
            0 -> band31 = value
            1 -> band62 = value
            2 -> band125 = value
            3 -> band250 = value
            4 -> band500 = value
            5 -> band1k = value
            6 -> band2k = value
            7 -> band4k = value
        }
        Log.d("DspViewModel", "updateBand($index, $value) called. Current dspReady: $dspReady")
        if (dspReady) {
            DspEngine.nativeSetEqBand(index, value)
            Log.d("DspViewModel", "Native call: DspEngine.nativeSetEqBand($index, $value) made.")
        } else {
            Log.d("DspViewModel", "Native call for band $index SKIPPED because dspReady is false.")
        }
        debouncedSave()
    }

    suspend fun onDspReady() {
        Log.d("DspViewModel", "onDspReady() called: Starting DSP initialization.")
        restoreState()
        dspReady = true
        syncToNative()
        Log.d("DspViewModel", "onDspReady() completed. dspReady is now: $dspReady")
    }

    fun updateLimiterEnabled(enabled: Boolean) {
        limiterEnabled = enabled
        if (dspReady) DspEngine.nativeSetLimiterEnabled(enabled)
        debouncedSave()
    }

    fun updateLimiterThreshold(value: Float) {
        limiterThreshold = value.coerceIn(0.1f, 1.0f)
        if (dspReady) DspEngine.nativeSetLimiterThreshold(limiterThreshold)
        debouncedSave()
    }

    fun updateReverbEnabled(enabled: Boolean) {
        reverbEnabled = enabled
        if (dspReady) DspEngine.nativeSetReverbEnabled(enabled)
        debouncedSave()
    }

    fun updateReverbLevel(level: Float) {
        // Coerce level to 0.0f-1.0f for native (e.g., wet/dry mix)
        reverbLevel = level.coerceIn(0.0f, 1.0f)
        if (dspReady) DspEngine.nativeSetReverbLevel(reverbLevel)
        debouncedSave()
    }

    fun flush() {
        if (dspReady) {
            DspEngine.nativeFlush()
        }
    }

    /* ---------- Throttled I/O ---------- */

    private fun debouncedSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500) // Wait for 500ms of inactivity
            prefs.save(
                DspState(
                    eqEnabled = eqEnabled,
                    masterGain = masterGain,
                    // --- NEW: Save Reverb State ---
                    reverbEnabled = reverbEnabled,
                    reverbLevel = reverbLevel,
                    bands = floatArrayOf(band31, band62, band125, band250, band500, band1k, band2k, band4k)
                )
            )
        }
    }

    private fun syncToNative() {
        if (!dspReady) return

        // Batch sync to ensure C++ state matches Kotlin exactly
        DspEngine.nativeSetEqEnabled(eqEnabled)
        DspEngine.nativeSetMasterGain(masterGain)
        DspEngine.nativeSetLimiterEnabled(limiterEnabled)
        DspEngine.nativeSetLimiterThreshold(limiterThreshold)
        DspEngine.nativeSetReverbEnabled(reverbEnabled)
        DspEngine.nativeSetReverbLevel(reverbLevel)

        val bands = floatArrayOf(band31, band62, band125, band250, band500, band1k, band2k, band4k)
        bands.forEachIndexed { i, value ->
            DspEngine.nativeSetEqBand(i, value)
        }
    }
    override fun onCleared() { // This onCleared() is for DspViewModel itself, not PlayerViewModel
        Log.d("DspViewModel", "DspViewModel.onCleared() is being called! Releasing native DSP resources.")
        dspReady = false
        DspEngine.nativeRelease() // Release native resources when the ViewModel itself is cleared
        super.onCleared()
    }

    fun onPlayerReleased() {
        Log.d("DspViewModel", "onPlayerReleased() called (from PlayerViewModel.onCleared). Not changing dspReady state or releasing native DSP here.")    }
}