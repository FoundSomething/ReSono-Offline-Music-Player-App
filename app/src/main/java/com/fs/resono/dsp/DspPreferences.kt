package com.fs.resono.dsp

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "dsp_state"
)

class DspPreferences(private val context: Context) {

    companion object {
        val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
        val MASTER_GAIN = floatPreferencesKey("master_gain")
        val LIMITER_ENABLED = booleanPreferencesKey("limiter_enabled")
        val LIMITER_THRESHOLD = floatPreferencesKey("limiter_threshold")

        // --- NEW: Reverb Keys ---
        val REVERB_ENABLED = booleanPreferencesKey("reverb_enabled")
        val REVERB_LEVEL = floatPreferencesKey("reverb_level")

        val BAND_31 = floatPreferencesKey("band_31")
        val BAND_62 = floatPreferencesKey("band_62")
        val BAND_125 = floatPreferencesKey("band_125")
        val BAND_250 = floatPreferencesKey("band_250")
        val BAND_500 = floatPreferencesKey("band_500")
        val BAND_1K = floatPreferencesKey("band_1k")
        val BAND_2K = floatPreferencesKey("band_2k")
        val BAND_4K = floatPreferencesKey("band_4k")
    }

    val dspState: Flow<DspState> =
        context.dataStore.data.map { prefs ->
            DspState(
                eqEnabled = prefs[EQ_ENABLED] ?: true,
                masterGain = prefs[MASTER_GAIN] ?: 1.0f,
                limiterEnabled = prefs[LIMITER_ENABLED] ?: true,
                limiterThreshold = prefs[LIMITER_THRESHOLD] ?: 1.0f, // Default 1.0f
                // --- NEW: Default Reverb State ---
                reverbEnabled = prefs[REVERB_ENABLED] ?: false,
                reverbLevel = prefs[REVERB_LEVEL] ?: 0.5f, // Default 0.0f
                bands = floatArrayOf(
                    prefs[BAND_31] ?: 0f,
                    prefs[BAND_62] ?: 0f,
                    prefs[BAND_125] ?: 0f,
                    prefs[BAND_250] ?: 0f,
                    prefs[BAND_500] ?: 0f,
                    prefs[BAND_1K] ?: 0f,
                    prefs[BAND_2K] ?: 0f,
                    prefs[BAND_4K] ?: 0f
                )
            )
        }

    suspend fun save(state: DspState) {
        context.dataStore.edit { prefs ->
            prefs[EQ_ENABLED] = state.eqEnabled
            prefs[MASTER_GAIN] = state.masterGain
            prefs[LIMITER_ENABLED] = state.limiterEnabled
            prefs[LIMITER_THRESHOLD] = state.limiterThreshold
            // --- NEW: Save Reverb State ---
            prefs[REVERB_ENABLED] = state.reverbEnabled
            prefs[REVERB_LEVEL] = state.reverbLevel

            prefs[BAND_31] = state.bands[0]
            prefs[BAND_62] = state.bands[1]
            prefs[BAND_125] = state.bands[2]
            prefs[BAND_250] = state.bands[3]
            prefs[BAND_500] = state.bands[4]
            prefs[BAND_1K] = state.bands[5]
            prefs[BAND_2K] = state.bands[6]
            prefs[BAND_4K] = state.bands[7]
        }
    }
}