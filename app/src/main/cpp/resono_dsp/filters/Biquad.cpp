
#include <math.h>
#include <algorithm>
#include "Biquad.h"

Biquad::Biquad() {
    z1 = 0.0f;
    z2 = 0.0f;
    calculate();
}
Biquad::Biquad(float sr) : sampleRate(sr) {
    z1 = 0.0f;
    z2 = 0.0f;
    calculate();
}
void Biquad::setSampleRate(float sr) {
    sampleRate = sr;
    calculate();
}
void Biquad::setType(BiquadType t) {
    type = t;
    calculate();
}
void Biquad::setFrequency(float freqHz) {
    frequency = freqHz;
    calculate();
}
void Biquad::setQ(float q) {
    Q = q;
    calculate();
}
void Biquad::setGainDb(float db) {
    gainDb = db;
    calculate();
}
void Biquad::configure(BiquadType t,
                       float freqHz,
                       float q,
                       float db) {
    type = t;
    frequency = freqHz;
    Q = q;
    gainDb = db;
    calculate();
}
void Biquad::reset() {
    z1 = 0.0f;
    z2 = 0.0f;
}
void Biquad::calculate() {
    if (sampleRate <= 0.0f) sampleRate = 44100.0f;
    float safeFreq = std::max(10.0f, std::min(frequency, sampleRate * 0.49f));
    const float A = powf(10.0f, gainDb / 40.0f);
    const float omega = 2.0f * M_PI * (frequency / sampleRate);
    const float sn = sinf(omega);
    const float cs = cosf(omega);
    const float alpha = sn / (2.0f * Q);

    float b0, b1n, b2n, a0n, a1n, a2n;

    switch (type) {
        case bq_type_lowpass:
            b0 = (1.0f - cs) * 0.5f;
            b1n = 1.0f - cs;
            b2n = (1.0f - cs) * 0.5f;
            a0n = 1.0f + alpha;
            a1n = -2.0f * cs;
            a2n = 1.0f - alpha;
            break;

        case bq_type_highpass:
            b0 = (1.0f + cs) * 0.5f;
            b1n = -(1.0f + cs);
            b2n = (1.0f + cs) * 0.5f;
            a0n = 1.0f + alpha;
            a1n = -2.0f * cs;
            a2n = 1.0f - alpha;
            break;

        case bq_type_bandpass:
            b0 = alpha;
            b1n = 0.0f;
            b2n = -alpha;
            a0n = 1.0f + alpha;
            a1n = -2.0f * cs;
            a2n = 1.0f - alpha;
            break;

        case bq_type_notch:
            b0 = 1.0f;
            b1n = -2.0f * cs;
            b2n = 1.0f;
            a0n = 1.0f + alpha;
            a1n = -2.0f * cs;
            a2n = 1.0f - alpha;
            break;

        case bq_type_peak:
            b0 = 1.0f + alpha * A;
            b1n = -2.0f * cs;
            b2n = 1.0f - alpha * A;
            a0n = 1.0f + alpha / A;
            a1n = -2.0f * cs;
            a2n = 1.0f - alpha / A;
            break;

        case bq_type_lowshelf: {
            float sqrtA = sqrtf(A);
            b0 =    A * ((A + 1) - (A - 1) * cs + 2 * sqrtA * alpha);
            b1n =  2 * A * ((A - 1) - (A + 1) * cs);
            b2n =    A * ((A + 1) - (A - 1) * cs - 2 * sqrtA * alpha);
            a0n =        (A + 1) + (A - 1) * cs + 2 * sqrtA * alpha;
            a1n =   -2 * ((A - 1) + (A + 1) * cs);
            a2n =        (A + 1) + (A - 1) * cs - 2 * sqrtA * alpha;
            break;
        }

        case bq_type_highshelf: {
            float sqrtA = sqrtf(A);
            b0 =    A * ((A + 1) + (A - 1) * cs + 2 * sqrtA * alpha);
            b1n = -2 * A * ((A - 1) + (A + 1) * cs);
            b2n =    A * ((A + 1) + (A - 1) * cs - 2 * sqrtA * alpha);
            a0n =        (A + 1) - (A - 1) * cs + 2 * sqrtA * alpha;
            a1n =    2 * ((A - 1) - (A + 1) * cs);
            a2n =        (A + 1) - (A - 1) * cs - 2 * sqrtA * alpha;
            break;
        }
    }
    if (fabs(a0n) < 1e-9f) a0n = 1.0f;

    // normalize
    a0 = b0 / a0n;
    a1 = b1n / a0n;
    a2 = b2n / a0n;
    b1 = a1n / a0n;
    b2 = a2n / a0n;
}