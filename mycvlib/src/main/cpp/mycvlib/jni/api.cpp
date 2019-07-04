//
// Created by Ghesn Sfeir on 01/07/2019.
//

#include <jni.h>

#include "mycvengine.h"

extern "C" {

JNIEXPORT jstring JNICALL
Java_mycvlib_sample_com_mycvlib_Engine_nGetVersionString(JNIEnv* env, jclass jEngineClass)
{
  mycvlib::MyCVEngine engine;
  return env->NewStringUTF(engine.getVersionString().c_str());
}

JNIEXPORT jint JNICALL
Java_mycvlib_sample_com_mycvlib_Engine_nGetAverageValue(JNIEnv *env, jclass type, jint rows, jint cols, jobject data)
{
  void* dataAddress = (void*) env->GetDirectBufferAddress(data);
  mycvlib::MyCVEngine engine;
  return engine.getAverageValue(rows, cols, dataAddress);
}

}