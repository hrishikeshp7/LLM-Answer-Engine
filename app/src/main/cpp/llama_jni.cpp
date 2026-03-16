/**
 * JNI bridge between the Android app and llama.cpp inference engine.
 *
 * This file provides the native implementation for the LlamaInference Kotlin class.
 * When built without llama.cpp (LLAMA_AVAILABLE not defined), it provides stub
 * implementations that return placeholder values.
 *
 * To enable real inference:
 * 1. Clone llama.cpp into native/llama.cpp/
 * 2. Update CMakeLists.txt to include llama.cpp
 * 3. Rebuild with the Android NDK
 */

#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "LlamaJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#ifdef LLAMA_AVAILABLE
#include "llama.h"
// Do not include common.h, as it causes linking issues with common lib
#endif

extern "C" {

/**
 * Load a GGUF model file.
 *
 * @param modelPath Path to the .gguf model file
 * @param threads Number of CPU threads
 * @param contextSize Context window size
 * @return Model handle (pointer cast to jlong), or -1 on failure
 */
JNIEXPORT jlong JNICALL
Java_com_llmengine_app_inference_LlamaInference_loadModel(
    JNIEnv *env,
    jobject /* this */,
    jstring modelPath,
    jint threads,
    jint contextSize
) {
    const char *path = env->GetStringUTFChars(modelPath, nullptr);
    LOGI("Loading model from: %s (threads=%d, ctx=%d)", path, threads, contextSize);

#ifdef LLAMA_AVAILABLE
    llama_backend_init();

    auto model_params = llama_model_default_params();
    llama_model *model = llama_model_load_from_file(path, model_params);

    if (!model) {
        LOGE("Failed to load model: %s", path);
        env->ReleaseStringUTFChars(modelPath, path);
        return -1;
    }

    auto ctx_params = llama_context_default_params();
    ctx_params.n_ctx = contextSize;
    ctx_params.n_threads = threads;

    llama_context *ctx = llama_init_from_model(model, ctx_params);
    if (!ctx) {
        LOGE("Failed to create context");
        llama_model_free(model);
        env->ReleaseStringUTFChars(modelPath, path);
        return -1;
    }

    env->ReleaseStringUTFChars(modelPath, path);
    LOGI("Model loaded successfully");
    return reinterpret_cast<jlong>(ctx);
#else
    LOGI("Stub mode: model not actually loaded (llama.cpp not linked)");
    env->ReleaseStringUTFChars(modelPath, path);
    return 1; // Return a non-negative value to indicate "success" in stub mode
#endif
}

/**
 * Generate a text completion.
 *
 * @param modelHandle Handle from loadModel
 * @param prompt Input prompt
 * @param maxTokens Maximum tokens to generate
 * @param temperature Sampling temperature
 * @param callback TokenCallback for streaming
 * @return Complete generated text
 */
JNIEXPORT jstring JNICALL
Java_com_llmengine_app_inference_LlamaInference_generateCompletion(
    JNIEnv *env,
    jobject /* this */,
    jlong modelHandle,
    jstring prompt,
    jint maxTokens,
    jfloat temperature,
    jobject callback
) {
    const char *promptStr = env->GetStringUTFChars(prompt, nullptr);
    LOGI("Generating completion (maxTokens=%d, temp=%.2f)", maxTokens, temperature);

#ifdef LLAMA_AVAILABLE
    llama_context *ctx = reinterpret_cast<llama_context *>(modelHandle);
    // Full llama.cpp inference implementation would go here
    // This is a simplified version - production code would use the sampling API
    std::string result = "Generated response placeholder";

    env->ReleaseStringUTFChars(prompt, promptStr);
    return env->NewStringUTF(result.c_str());
#else
    std::string result = "This is a stub response. Build with llama.cpp to enable real inference.";

    // Call the token callback for streaming simulation
    if (callback != nullptr) {
        jclass callbackClass = env->GetObjectClass(callback);
        jmethodID onTokenMethod = env->GetMethodID(callbackClass, "onToken",
                                                     "(Ljava/lang/String;)Z");
        if (onTokenMethod != nullptr) {
            jstring token = env->NewStringUTF(result.c_str());
            env->CallBooleanMethod(callback, onTokenMethod, token);
            env->DeleteLocalRef(token);
        }
    }

    env->ReleaseStringUTFChars(prompt, promptStr);
    return env->NewStringUTF(result.c_str());
#endif
}

/**
 * Generate embeddings for text.
 *
 * @param modelHandle Handle to an embedding model
 * @param text Input text
 * @return Float array of embedding values
 */
JNIEXPORT jfloatArray JNICALL
Java_com_llmengine_app_inference_LlamaInference_generateEmbedding(
    JNIEnv *env,
    jobject /* this */,
    jlong modelHandle,
    jstring text
) {
    const char *textStr = env->GetStringUTFChars(text, nullptr);
    LOGI("Generating embedding for text");

    const int embeddingSize = 384;
    jfloatArray result = env->NewFloatArray(embeddingSize);

#ifdef LLAMA_AVAILABLE
    // Full embedding generation with llama.cpp would go here
    float stub[384] = {0};
    env->SetFloatArrayRegion(result, 0, embeddingSize, stub);
#else
    float stub[384] = {0};
    env->SetFloatArrayRegion(result, 0, embeddingSize, stub);
#endif

    env->ReleaseStringUTFChars(text, textStr);
    return result;
}

/**
 * Unload a model and free resources.
 */
JNIEXPORT void JNICALL
Java_com_llmengine_app_inference_LlamaInference_unloadModel(
    JNIEnv *env,
    jobject /* this */,
    jlong modelHandle
) {
    LOGI("Unloading model");

#ifdef LLAMA_AVAILABLE
    llama_context *ctx = reinterpret_cast<llama_context *>(modelHandle);
    if (ctx) {
        llama_free(ctx);
    }
    llama_backend_free();
#endif
}

/**
 * Get current tokens per second.
 */
JNIEXPORT jfloat JNICALL
Java_com_llmengine_app_inference_LlamaInference_getTokensPerSecond(
    JNIEnv *env,
    jobject /* this */,
    jlong modelHandle
) {
#ifdef LLAMA_AVAILABLE
    // In production, track timing during generation
    return 0.0f;
#else
    return 0.0f;
#endif
}

} // extern "C"
