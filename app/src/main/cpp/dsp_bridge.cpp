#include <jni.h>
#include "dsp_engine.h"

extern "C" {
JNIEXPORT void JNICALL Java_com_fs_resono_dsp_DspEngine_nativeInit(JNIEnv* env, jobject, jint sr, jint ch) {
    dsp::init(sr, ch);
}

JNIEXPORT void JNICALL Java_com_fs_resono_dsp_DspEngine_nativeProcessPcm(JNIEnv* env, jobject, jfloatArray pcmArray, jint samples) {
    if (!pcmArray) return;
    jfloat* pcmData = env->GetFloatArrayElements(pcmArray, nullptr);
    if (pcmData) {
        dsp::process(pcmData, (size_t)samples);
        env->ReleaseFloatArrayElements(pcmArray, pcmData, 0); // 0 = Copy back and release
    }
}
JNIEXPORT void JNICALL
Java_com_fs_resono_dsp_DspEngine_nativeSetEqBand(JNIEnv* env, jobject, jint index, jfloat gain)
{
    dsp::setEqBand(index, gain);
}
JNIEXPORT void JNICALL
Java_com_fs_resono_dsp_DspEngine_nativeRelease(JNIEnv* env, jobject)
{
    dsp::release();
}
JNIEXPORT void JNICALL
Java_com_fs_resono_dsp_DspEngine_nativeFlush(JNIEnv* env, jobject)
{
    dsp::flush();
}
JNIEXPORT void JNICALL
Java_com_fs_resono_dsp_DspEngine_nativeSetEqEnabled(JNIEnv* env, jobject, jboolean e)
{
    dsp::setEqEnabled(e);
}
JNIEXPORT void JNICALL
Java_com_fs_resono_dsp_DspEngine_nativeSetMasterGain(JNIEnv* env, jobject, jfloat g)
{
    dsp::setMasterGain(g);
}
JNIEXPORT void JNICALL
Java_com_fs_resono_dsp_DspEngine_nativeSetLimiterEnabled(JNIEnv *env, jobject thiz, jboolean enabled)
{
    dsp::setLimiterEnabled(enabled);
}
JNIEXPORT void JNICALL
Java_com_fs_resono_dsp_DspEngine_nativeSetLimiterThreshold(JNIEnv *env, jobject thiz, jfloat threshold)
{
    dsp::setLimiterThreshold(threshold);
}
JNIEXPORT void JNICALL
Java_com_fs_resono_dsp_DspEngine_nativeSetReverbEnabled(JNIEnv *env, jobject clazz, jboolean enabled) {
    dsp::setReverbEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_fs_resono_dsp_DspEngine_nativeSetReverbLevel(JNIEnv *env, jobject clazz, jfloat level) {
    dsp::setReverbLevel(level);
}
}