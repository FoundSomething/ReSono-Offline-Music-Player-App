#include "dsp_engine.h"
#include "Biquad.h"
#include <vector>
#include <mutex>
#include <atomic>
#include <algorithm>
#include <cmath>

namespace dsp {
    static int gSampleRate = 44100;
    static int gChannels = 2;
    static std::recursive_mutex gDspMutex;
    static std::atomic<bool> gInitialized{false};

    static std::atomic<bool> eqEnabled{true};
    static std::atomic<float> masterGain{1.0f};
    static std::atomic<bool> limiterEnabled{true};
    static std::atomic<float> limiterThreshold{1.0f};

    static std::vector<Biquad> leftEQ;
    static std::vector<Biquad> rightEQ;

    static constexpr int NUM_BANDS = 8;
    static float bandFreqs[NUM_BANDS] = {31.f, 62.f, 125.f, 250.f, 500.f, 1000.f, 2000.f, 4000.f};

    static std::atomic<bool> reverbEnabled{false};
    static std::atomic<float> reverbLevel{0.0f}; // 0.0f (dry) to 1.0f (wet)

    static std::vector<float> delayBufferL;
    static std::vector<float> delayBufferR;
    static int delayWritePos = 0;
    static constexpr int MAX_DELAY_SAMPLES = 44100 * 2;
    static constexpr float DELAY_TIME_MS = 250.0f;
    static constexpr float FEEDBACK_GAIN = 0.7f;

    static std::vector<float> allPassBufferL;
    static std::vector<float> allPassBufferR;
    static int allPassWritePos = 0;
    static constexpr int ALL_PASS_DELAY_SAMPLES = 44100 / 33; // ~30ms delay for all-pass
    static constexpr float ALL_PASS_GAIN = 0.707f; // Common gain for all-pass feedback/feedforward

    static constexpr float INTERNAL_DELAY_CLIP_THRESHOLD = 0.9f;

    void init(int sampleRate, int channels) {
        std::lock_guard<std::recursive_mutex> lock(gDspMutex);
        gSampleRate = sampleRate;
        gChannels = channels;

        leftEQ.assign(NUM_BANDS, Biquad());
        rightEQ.assign(NUM_BANDS, Biquad());
        for (int i = 0; i < NUM_BANDS; ++i) {
            leftEQ[i].setSampleRate(sampleRate);
            rightEQ[i].setSampleRate(sampleRate);
            leftEQ[i].configure(bq_type_peak, bandFreqs[i], 1.0f, 0.0f);
            rightEQ[i].configure(bq_type_peak, bandFreqs[i], 1.0f, 0.0f);
        }
        delayBufferL.assign(MAX_DELAY_SAMPLES, 0.0f);
        delayBufferR.assign(MAX_DELAY_SAMPLES, 0.0f);
        delayWritePos = 0;

        allPassBufferL.assign(ALL_PASS_DELAY_SAMPLES, 0.0f);
        allPassBufferR.assign(ALL_PASS_DELAY_SAMPLES, 0.0f);
        allPassWritePos = 0;

        gInitialized.store(true);
    }

    void setEqBand(int index, float gainDb) {
        if (!gInitialized.load()) return;
        if (index < 0 || index >= NUM_BANDS) return;

        std::lock_guard<std::recursive_mutex> lock(gDspMutex);
        // CRITICAL: Must re-configure to update internal filter coefficients
        leftEQ[index].configure(bq_type_peak, bandFreqs[index], 1.0f, gainDb);
        rightEQ[index].configure(bq_type_peak, bandFreqs[index], 1.0f, gainDb);
    }

    void process(float* pcm, size_t samples) {
        if (!gInitialized.load() || !pcm) return;

        // Use try_lock on the audio thread to prevent Signal 11 if the UI blocks
        if (!gDspMutex.try_lock()) return;

        const bool eqOn = eqEnabled.load();
        const float mGain = masterGain.load();
        const float thresh = limiterThreshold.load();
        const bool limitOn = limiterEnabled.load();
        const bool revOn = reverbEnabled.load();
        const float revWetLevel = reverbLevel.load(); // Your 0.0f to 1.0f slider value

        int delayReadOffset = static_cast<int>(DELAY_TIME_MS / 1000.0f * gSampleRate);
        if (delayReadOffset >= MAX_DELAY_SAMPLES) delayReadOffset = MAX_DELAY_SAMPLES - 1;

        int allPassDelayReadOffset = ALL_PASS_DELAY_SAMPLES;

        for (size_t i = 0; i < samples; i += gChannels) {
            float l = pcm[i];
            float r = (gChannels > 1) ? pcm[i + 1] : l;

            if (eqOn) {
                for (int b = 0; b < NUM_BANDS; ++b) {
                    l = leftEQ[b].process(l);
                    if (gChannels > 1) r = rightEQ[b].process(r);
                }
                l *= mGain;
                r *= mGain;
            }
            if (revOn && revWetLevel > 0.0f) {
                float dryL = l;
                float dryR = r;

                int currentDelayReadPos = (delayWritePos - delayReadOffset + MAX_DELAY_SAMPLES) % MAX_DELAY_SAMPLES;
                float wetL = delayBufferL[currentDelayReadPos];
                float wetR = delayBufferR[currentDelayReadPos];

                float processedL = dryL + wetL * FEEDBACK_GAIN;
                float processedR = dryR + wetR * FEEDBACK_GAIN;

                processedL = INTERNAL_DELAY_CLIP_THRESHOLD * std::tanh(processedL / INTERNAL_DELAY_CLIP_THRESHOLD);
                processedR = INTERNAL_DELAY_CLIP_THRESHOLD * std::tanh(processedR / INTERNAL_DELAY_CLIP_THRESHOLD);

                delayBufferL[delayWritePos] = processedL;
                delayBufferR[delayWritePos] = processedR;

                // --- 2. All-Pass Filter (takes output of comb filter as input) ---
                // y[n] = -x[n] + x[n - D] + G * y[n - D]
                int currentAllPassReadPos = (allPassWritePos - allPassDelayReadOffset + ALL_PASS_DELAY_SAMPLES) % ALL_PASS_DELAY_SAMPLES;

                // Get delayed input for all-pass
                float allPassDelayedInputL = allPassBufferL[currentAllPassReadPos];
                float allPassDelayedInputR = allPassBufferR[currentAllPassReadPos];

                // Get delayed output for all-pass (which is stored in the buffer)
                float allPassDelayedOutputL = allPassBufferL[currentAllPassReadPos]; // For all-pass, buffer stores both
                float allPassDelayedOutputR = allPassBufferR[currentAllPassReadPos];


                // Calculate all-pass output
                float allPassOutputL = (-wetL) + allPassDelayedInputL + (ALL_PASS_GAIN * allPassDelayedOutputL);
                float allPassOutputR = (-wetR) + allPassDelayedInputR + (ALL_PASS_GAIN * allPassDelayedOutputR);

                // Store current input for next delayed input read, and current output for next delayed output read
                allPassBufferL[allPassWritePos] = wetL + (ALL_PASS_GAIN * allPassOutputL);
                allPassBufferR[allPassWritePos] = wetR + (ALL_PASS_GAIN * allPassOutputR);
                // Note: The above write is actually storing the *feedback* signal for the all-pass structure.
                // It's a slightly different structure for all-pass. Let's simplify this.

                // --- Simpler All-Pass Structure for DSP ---
                // y(n) = g*x(n) + x(n-D) - g*y(n-D) where g is ALL_PASS_GAIN
                float allPassInputL = wetL;
                float allPassInputR = wetR;

                float allPassFeedbackL = allPassBufferL[currentAllPassReadPos];
                float allPassFeedbackR = allPassBufferR[currentAllPassReadPos];

                float wetOutputL = (ALL_PASS_GAIN * allPassInputL) + allPassFeedbackL - (ALL_PASS_GAIN * allPassFeedbackL);
                float wetOutputR = (ALL_PASS_GAIN * allPassInputR) + allPassFeedbackR - (ALL_PASS_GAIN * allPassFeedbackR);

                allPassBufferL[allPassWritePos] = allPassInputL - (ALL_PASS_GAIN * wetOutputL);
                allPassBufferR[allPassWritePos] = allPassInputR - (ALL_PASS_GAIN * wetOutputR);


                l = (dryL * (1.0f - revWetLevel)) + (wetL * revWetLevel);
                r = (dryR * (1.0f - revWetLevel)) + (wetR * revWetLevel);
            }

            if (limitOn) {
                if (thresh > 0.0001f) {
                    l = thresh * std::tanh(l / thresh);
                    if (gChannels > 1) r = thresh * std::tanh(r / thresh);
                } else {
                    l = std::max(-0.0001f, std::min(0.0001f, l));
                    if (gChannels > 1) r = std::max(-0.0001f, std::min(0.0001f, r));
                }
            }

            pcm[i] = l;
            if (gChannels > 1) pcm[i + 1] = r;
            delayWritePos = (delayWritePos + 1) % MAX_DELAY_SAMPLES;
        }
        gDspMutex.unlock();
    }

    void flush() {
        std::lock_guard<std::recursive_mutex> lock(gDspMutex);
        if (!gInitialized) return;

        for (auto& filter : leftEQ) filter.reset();
        for (auto& filter : rightEQ) filter.reset();

        std::fill(delayBufferL.begin(), delayBufferL.end(), 0.0f);
        std::fill(delayBufferR.begin(), delayBufferR.end(), 0.0f);
        delayWritePos = 0;
        std::fill(allPassBufferL.begin(), allPassBufferL.end(), 0.0f);
        std::fill(allPassBufferR.begin(), allPassBufferR.end(), 0.0f);
        allPassWritePos = 0;
    }

    void release() {
        gInitialized.store(false);
        std::lock_guard<std::recursive_mutex> lock(gDspMutex);
        leftEQ.clear();
        rightEQ.clear();

        delayBufferL.clear();
        delayBufferR.clear();
        allPassBufferL.clear();
        allPassBufferR.clear();
    }

    // Atomically simple setters
    void setEqEnabled(bool enabled) { eqEnabled.store(enabled); }
    void setMasterGain(float gain) { masterGain.store(gain); }
    void setLimiterEnabled(bool enabled) { limiterEnabled.store(enabled); }
    void setLimiterThreshold(float threshold) { limiterThreshold.store(threshold); }
    void setReverbEnabled(bool enabled) { reverbEnabled.store(enabled); }
    void setReverbLevel(float level) { reverbLevel.store(level); }
}