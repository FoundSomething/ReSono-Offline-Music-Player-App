//
//  Biquad.h
//
//  Created by Nigel Redmon on 11/24/12
//  EarLevel Engineering: earlevel.com
//  Copyright 2012 Nigel Redmon
//
//  For a complete explanation of the Biquad code:
//  http://www.earlevel.com/main/2012/11/25/biquad-c-source-code/
//
//  License:
//
//  This source code is provided as is, without warranty.
//  You may copy and distribute verbatim copies of this document.
//  You may modify and use this source code to create binary code
//  for your own purposes, free or commercial.
//

#ifndef Biquad_h
#define Biquad_h

#include <cmath>
#include <cfloat>


enum BiquadType {
    bq_type_lowpass = 0,
    bq_type_highpass,
    bq_type_bandpass,
    bq_type_notch,
    bq_type_peak,
    bq_type_lowshelf,
    bq_type_highshelf
};

class Biquad {
public:
    Biquad();
    Biquad(float sampleRate);

    void setSampleRate(float sr);
    void setType(BiquadType type);
    void setFrequency(float FreqHz);
    void setQ(float Q);
    void configure(BiquadType type, float freqHz, float q, float gainDb);
    void setGainDb(float gainDb);

    void reset();
    inline float process(float in);

private:
    void calculate();

    BiquadType type = bq_type_lowpass;

    float sampleRate = 48000.0f;
    float frequency = 1000.0f;
    float Q = 0.707f;
    float gainDb = 0.0f;

    float a0 = 1.0f, a1 = 0.0f, a2 = 0.0f;
    float b1 = 0.0f, b2 = 0.0f;

    float z1 = 0.0f, z2 = 0.0f;
};

inline float Biquad::process(float in) {
    float out = in * a0 + z1;
    z1 = in * a1 + z2 - b1 * out;
    z2 = in * a2 - b2 * out;

    // denormal protection
    if (fabsf(z1) < 1e-9f) z1 = 0.0f;
    if (fabsf(z2) < 1e-9f) z2 = 0.0f;

    return out;
}

#endif // Biquad_h
