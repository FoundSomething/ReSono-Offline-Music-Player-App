package com.fs.resono.dsp

object DspEngine {

    init {
        System.loadLibrary("resono_dsp")
    }

    /* ---------- Lifecycle ---------- */
    external fun nativeInit(sampleRate: Int, channels: Int)
    external fun nativeFlush()
    external fun nativeRelease()
    /* ---------- Real-time DSP ---------- */
    external fun nativeProcessPcm(dsp : FloatArray, frames: Int)
    /* ---------- DSP Parameters ---------- */
    external fun nativeSetEqEnabled(enabled: Boolean)
    external fun nativeSetEqBand(band: Int, gainDb: Float)
    external fun nativeSetMasterGain(gain: Float)

    /* ---------- Limiter ---------- */
    external fun nativeSetLimiterEnabled(enabled: Boolean)
    external fun nativeSetLimiterThreshold(threshold: Float)

    external fun nativeSetReverbEnabled(enabled: Boolean)
    external fun nativeSetReverbLevel(level: Float)


}
