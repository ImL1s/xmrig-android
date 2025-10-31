#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>
#include <sys/sysconf.h>

#define LOG_TAG "XMRigBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_iml1s_xmrigminer_native_XMRigBridge_getVersion(
    JNIEnv* env,
    jobject /* this */) {
    LOGI("Getting XMRig version");
    return env->NewStringUTF("XMRig 6.21.0 (Android Custom Build)");
}

JNIEXPORT jint JNICALL
Java_com_iml1s_xmrigminer_native_XMRigBridge_getCpuCores(
    JNIEnv* env,
    jobject /* this */) {
    int cores = sysconf(_SC_NPROCESSORS_CONF);
    LOGI("CPU cores detected: %d", cores);
    return cores;
}

JNIEXPORT jstring JNICALL
Java_com_iml1s_xmrigminer_native_XMRigBridge_getCpuInfo(
    JNIEnv* env,
    jobject /* this */) {
    int cores = sysconf(_SC_NPROCESSORS_CONF);
    
    std::string info = "Cores: " + std::to_string(cores);
    
    #ifdef __aarch64__
        info += ", Arch: ARM64 (AArch64)";
    #elif defined(__arm__)
        info += ", Arch: ARM32";
    #else
        info += ", Arch: Unknown";
    #endif
    
    LOGI("CPU Info: %s", info.c_str());
    return env->NewStringUTF(info.c_str());
}

JNIEXPORT jboolean JNICALL
Java_com_iml1s_xmrigminer_native_XMRigBridge_hasCryptoExtensions(
    JNIEnv* env,
    jobject /* this */) {
    #ifdef __aarch64__
        // ARM64 通常支持加密擴展
        return JNI_TRUE;
    #else
        return JNI_FALSE;
    #endif
}

} // extern "C"
