
#ifndef RESONO_DSP_ENGINE_H
#define RESONO_DSP_ENGINE_H
#pragma once

#include <stddef.h>

namespace dsp {

    void init(int sampleRate, int channels);
    void process(float* pcm, size_t frames);
    void flush();
    void release();

    void setEqEnabled(bool enabled);
    void setEqBand(int bandIndex, float gainDb);
    void setMasterGain(float gain);

    void setLimiterEnabled(bool enabled);
    void setLimiterThreshold(float threshold);
    void setReverbEnabled(bool enabled);
    void setReverbLevel(float level);
};
#endif //RESONO_DSP_ENGINE_H
