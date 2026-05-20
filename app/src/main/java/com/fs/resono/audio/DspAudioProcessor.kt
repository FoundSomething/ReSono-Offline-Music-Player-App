package com.fs.resono.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import com.fs.resono.dsp.DspEngine
import java.nio.ByteBuffer
import java.nio.ByteOrder

@UnstableApi
class DspAudioProcessor(private val onReady: () -> Unit) : AudioProcessor {
    private var inputFormat = AudioProcessor.AudioFormat.NOT_SET
    private var pcmBuffer = FloatArray(0)
    private var outputBuffer = AudioProcessor.EMPTY_BUFFER
    private var tempBuffer = AudioProcessor.EMPTY_BUFFER
    private var isEnded = false

    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT && inputAudioFormat.encoding != C.ENCODING_PCM_FLOAT) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }

        if (inputFormat != inputAudioFormat) {
            inputFormat = inputAudioFormat
            DspEngine.nativeInit(inputFormat.sampleRate, inputFormat.channelCount)
            onReady()
        }
        return inputFormat
    }

    override fun isActive(): Boolean = inputFormat != AudioProcessor.AudioFormat.NOT_SET

    override fun queueInput(inputBuffer: ByteBuffer) {
        val count = inputBuffer.remaining()
        if (count == 0) return

        val bytesPerSample = if (inputFormat.encoding == C.ENCODING_PCM_FLOAT) 4 else 2
        val numSamples = count / bytesPerSample

        if (pcmBuffer.size < numSamples) pcmBuffer = FloatArray(numSamples)

        // 1. Convert to Float
        inputBuffer.order(ByteOrder.nativeOrder())
        if (inputFormat.encoding == C.ENCODING_PCM_FLOAT) {
            inputBuffer.asFloatBuffer().get(pcmBuffer, 0, numSamples)
        } else {
            for (i in 0 until numSamples) {
                pcmBuffer[i] = inputBuffer.short / 32768f
            }
        }

        // 2. NATIVE PROCESSING
        DspEngine.nativeProcessPcm(pcmBuffer, numSamples)

        // 3. Prepare Output
        if (tempBuffer.capacity() < count) {
            tempBuffer = ByteBuffer.allocateDirect(count).order(ByteOrder.nativeOrder())
        }
        tempBuffer.clear()

        if (inputFormat.encoding == C.ENCODING_PCM_FLOAT) {
            tempBuffer.asFloatBuffer().put(pcmBuffer, 0, numSamples)
        } else {
            for (i in 0 until numSamples) {
                val s = (pcmBuffer[i] * 32767f).toInt().coerceIn(-32768, 32767)
                tempBuffer.putShort(s.toShort())
            }
        }

        tempBuffer.flip()
        outputBuffer = tempBuffer
    }

    override fun getOutput(): ByteBuffer {
        val out = outputBuffer
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        return out
    }

    override fun flush() {
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        isEnded = false
        DspEngine.nativeFlush()
    }

    override fun reset() {
        flush()
        inputFormat = AudioProcessor.AudioFormat.NOT_SET
        pcmBuffer = FloatArray(0)
    }

    override fun queueEndOfStream() { isEnded = true }
    override fun isEnded(): Boolean = isEnded && outputBuffer == AudioProcessor.EMPTY_BUFFER
}