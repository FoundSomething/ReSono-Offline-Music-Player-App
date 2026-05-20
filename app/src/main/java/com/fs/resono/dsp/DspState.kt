package com.fs.resono.dsp

data class DspState(
    val eqEnabled: Boolean = true,
    val masterGain: Float = 1.0f,
    val limiterEnabled: Boolean = true,
    val limiterThreshold: Float = 1.0f,
    // --- NEW: Reverb State ---
    val reverbEnabled: Boolean = false,
    val reverbLevel: Float = 0.0f, // Default 0.0f
    val bands: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DspState

        if (eqEnabled != other.eqEnabled) return false
        if (masterGain != other.masterGain) return false
        if (limiterEnabled != other.limiterEnabled) return false
        if (limiterThreshold != other.limiterThreshold) return false
        if (reverbEnabled != other.reverbEnabled) return false // NEW
        if (reverbLevel != other.reverbLevel) return false     // NEW
        if (!bands.contentEquals(other.bands)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = eqEnabled.hashCode()
        result = 31 * result + masterGain.hashCode()
        result = 31 * result + limiterEnabled.hashCode()
        result = 31 * result + limiterThreshold.hashCode()
        result = 31 * result + reverbEnabled.hashCode() // NEW
        result = 31 * result + reverbLevel.hashCode()     // NEW
        result = 31 * result + bands.contentHashCode()
        return result
    }
}