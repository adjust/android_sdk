#include <jni.h>

extern "C"
jstring
Java_com_adjust_sdk_DeviceInfo_nativeISA(
        JNIEnv *env,
        jobject /* this */) {
    const char* isa = "";
#if defined(__arm__)
    isa = "arm";
#elif defined(__aarch64__)
    isa = "arm64";
#elif defined(__mips__) && !defined(__LP64__)
    isa = "mips";
#elif defined(__mips__) && defined(__LP64__)
    isa = "mips64";
#elif defined(__i386__)
    isa = "x86";
#elif defined(__x86_64__)
    isa = "x86_64";
#else
    isa= "none";
#endif
    return env->NewStringUTF(isa);
}
