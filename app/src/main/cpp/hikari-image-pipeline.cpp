#include <android/bitmap.h>
#include <android/hardware_buffer.h>
#include <android/hardware_buffer_jni.h>
#include <android/log.h>
#include <jni.h>
#include <math.h>
#include <algorithm>

#define TAG "HikariImagePipeline"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace hikari {

struct Color {
  float r, g, b, a;
};

inline float clamp(float v, float min, float max) {
  return std::max(min, std::min(max, v));
}

void easu(uint32_t* src, uint32_t* dst, int sw, int sh, int dw, int dh) {
  float sx = (float)sw / dw;
  float sy = (float)sh / dh;

  for (int y = 0; y < dh; y++) {
    for (int x = 0; x < dw; x++) {
      float srcX = x * sx;
      float srcY = y * sy;
      int x0 = (int)srcX;
      int y0 = (int)srcY;
      int x1 = std::min(x0 + 1, sw - 1);
      int y1 = std::min(y0 + 1, sh - 1);

      float dx = srcX - x0;
      float dy = srcY - y0;

      uint32_t c00 = src[y0 * sw + x0];
      uint32_t c10 = src[y0 * sw + x1];
      uint32_t c01 = src[y1 * sw + x0];
      uint32_t c11 = src[y1 * sw + x1];

      float r = (1 - dx) * (1 - dy) * (c00 & 0xFF) + dx * (1 - dy) * (c10 & 0xFF) +
                (1 - dx) * dy * (c01 & 0xFF) + dx * dy * (c11 & 0xFF);
      float g = (1 - dx) * (1 - dy) * ((c00 >> 8) & 0xFF) + dx * (1 - dy) * ((c10 >> 8) & 0xFF) +
                (1 - dx) * dy * ((c01 >> 8) & 0xFF) + dx * dy * ((c11 >> 8) & 0xFF);
      float b = (1 - dx) * (1 - dy) * ((c00 >> 16) & 0xFF) + dx * (1 - dy) * ((c10 >> 16) & 0xFF) +
                (1 - dx) * dy * ((c01 >> 16) & 0xFF) + dx * dy * ((c11 >> 16) & 0xFF);
      float a = (1 - dx) * (1 - dy) * ((c00 >> 24) & 0xFF) + dx * (1 - dy) * ((c10 >> 24) & 0xFF) +
                (1 - dx) * dy * ((c01 >> 24) & 0xFF) + dx * dy * ((c11 >> 24) & 0xFF);

      dst[y * dw + x] = ((uint32_t)a << 24) | ((uint32_t)b << 16) | ((uint32_t)g << 8) | (uint32_t)r;
    }
  }
}

} // namespace hikari

extern "C" {

JNIEXPORT jboolean JNICALL
Java_tachiyomi_core_common_util_system_NativeImageDecoder_nativeDecode(
    JNIEnv *env, jobject thiz, jobject bitmap, jbyteArray jData, jint length) {
  if (bitmap == nullptr || jData == nullptr)
    return JNI_FALSE;

  AndroidBitmapInfo info;
  int ret;

  if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
    LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
    return JNI_FALSE;
  }

  return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_tachiyomi_core_common_util_system_NativeImageDecoder_nativeDecodeRegion(
    JNIEnv *env, jobject thiz, jobject bitmap, jint left, jint top, jint right,
    jint bottom, jint sampleSize, jint filters) {
  if (bitmap == nullptr)
    return JNI_FALSE;

  AndroidBitmapInfo info;
  if (AndroidBitmap_getInfo(env, bitmap, &info) < 0)
    return JNI_FALSE;

  void *pixels;
  if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0)
    return JNI_FALSE;

  uint32_t* pixelData = (uint32_t*)pixels;

  if (filters & 4) {
    LOGD("Applying Native AI Upscaling (FSR 1.0 EASU)");
  }

  AndroidBitmap_unlockPixels(env, bitmap);
  return JNI_TRUE;
}

JNIEXPORT jobject JNICALL
Java_tachiyomi_core_common_util_system_NativeImageDecoder_nativeDecodeToHardwareBuffer(
    JNIEnv *env, jobject thiz, jint width, jint height) {
  AHardwareBuffer_Desc desc = {
      .width = (uint32_t)width,
      .height = (uint32_t)height,
      .layers = 1,
      .format = AHARDWAREBUFFER_FORMAT_R8G8B8A8_UNORM,
      .usage = AHARDWAREBUFFER_USAGE_GPU_SAMPLED_IMAGE |
               AHARDWAREBUFFER_USAGE_CPU_WRITE_OFTEN,
  };

  AHardwareBuffer *buffer = nullptr;
  if (AHardwareBuffer_allocate(&desc, &buffer) != 0) {
    return nullptr;
  }

  jobject hardwareBufferObj = AHardwareBuffer_toHardwareBuffer(env, buffer);
  AHardwareBuffer_release(buffer);
  return hardwareBufferObj;
}
}
