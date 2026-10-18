// Gives libretro cores that expect to run inside their own Android app the JavaVM they need.
//
// Play! (PS2) attaches its emulation thread to the JVM through Framework::CJavaVM, whose
// pointer is normally set by Play!'s own Android app. A libretro frontend that loads the
// core with dlopen never sets it, so the core dereferences null on startup. We load the
// core first, hand it the JavaVM, and keep the handle open so the frontend's later dlopen
// of the same path reuses this already-initialised instance.

#include <dlfcn.h>
#include <jni.h>
#include <android/log.h>

#define LOG_TAG "2CCoreBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static JavaVM* g_vm = nullptr;

extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void*) {
    g_vm = vm;
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_swordfish_lemuroid_app_shared_game_CoreBridge_provideJavaVm(
    JNIEnv* env, jobject, jstring corePath, jstring symbol) {
    const char* path = env->GetStringUTFChars(corePath, nullptr);
    const char* name = env->GetStringUTFChars(symbol, nullptr);

    jboolean result = JNI_FALSE;
    void* handle = dlopen(path, RTLD_NOW);
    if (handle == nullptr) {
        LOGE("dlopen(%s) failed: %s", path, dlerror());
    } else {
        using SetJavaVmFn = void (*)(JavaVM*);
        auto setJavaVm = reinterpret_cast<SetJavaVmFn>(dlsym(handle, name));
        if (setJavaVm == nullptr) {
            LOGE("Symbol %s not found in %s", name, path);
        } else {
            setJavaVm(g_vm);
            LOGI("Provided JavaVM to %s", path);
            result = JNI_TRUE;
        }
        // Intentionally never dlclose: the core must stay loaded with its JavaVM set.
    }

    env->ReleaseStringUTFChars(symbol, name);
    env->ReleaseStringUTFChars(corePath, path);
    return result;
}
