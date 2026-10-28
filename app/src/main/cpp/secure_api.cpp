#include <jni.h>
#include <cstring>
#include <string>

/**
 * Base URL is stored XOR-obfuscated (not plaintext).
 * Decoded only in native memory at runtime.
 * Note: determined reverse engineers with a debugger can still recover it;
 * this blocks casual APK string dumps / jadx search.
 */

namespace {

constexpr unsigned char kKey[] = {
        0xA7, 0x3C, 0x91, 0x5E, 0x2B, 0xD4, 0x68, 0xF1
};

// Obfuscated API host bytes (decoded only at runtime)
constexpr unsigned char kEnc[] = {
        0xE4, 0x74, 0xA8, 0x70, 0x37, 0x6E, 0xD6, 0x7C,
        0x76, 0x94, 0x25, 0xCB, 0xA8, 0xB1, 0x1E, 0xB3,
        0xFD, 0x1E, 0xA0, 0x5F, 0x35, 0x2A, 0xE7, 0x22,
        0x0A, 0x9C, 0x1C, 0xCD, 0x41, 0xA7, 0x2F, 0xA2,
        0x8B, 0x08, 0x88, 0x0E, 0xD0, 0x11, 0xBA, 0x5B,
        0x5B
};

constexpr size_t kLen = sizeof(kEnc);

std::string decodeBase() {
    std::string out;
    out.resize(kLen);
    for (size_t i = 0; i < kLen; ++i) {
        unsigned char mix = static_cast<unsigned char>((i * 17 + 43) & 0xFF);
        out[i] = static_cast<char>(kEnc[i] ^ kKey[i % sizeof(kKey)] ^ mix);
    }
    return out;
}

}  // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_com_loan_emi_calculator_loanemicalc_finance_bankingcalculator_financialcalculator_ads_SecureApi_nativeBaseUrl(
        JNIEnv *env, jclass /*clazz*/) {
    std::string url = decodeBase();
    jstring result = env->NewStringUTF(url.c_str());
    // Best-effort wipe stack buffer
    volatile char *p = &url[0];
    for (size_t i = 0; i < url.size(); ++i) {
        p[i] = 0;
    }
    return result;
}
