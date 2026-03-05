package com.llmengine.app.inference

/**
 * JNI bridge to llama.cpp for local model inference.
 *
 * This class wraps the native C++ inference engine and provides a Kotlin API
 * for loading models and generating text completions.
 *
 * The native library (libllama_jni.so) must be compiled from the C++ sources
 * in app/src/main/cpp/ using the Android NDK.
 */
class LlamaInference {

    companion object {
        init {
            try {
                System.loadLibrary("llama_jni")
            } catch (e: UnsatisfiedLinkError) {
                // Native library not available - will use stub implementation
                android.util.Log.w("LlamaInference", "Native library not loaded: ${e.message}")
            }
        }
    }

    /**
     * Initialize the inference engine with a model file.
     *
     * @param modelPath Absolute path to the GGUF model file
     * @param threads Number of CPU threads to use
     * @param contextSize Context window size in tokens
     * @return Handle to the loaded model, or -1 on failure
     */
    external fun loadModel(modelPath: String, threads: Int, contextSize: Int): Long

    /**
     * Generate a text completion for the given prompt.
     *
     * @param modelHandle Handle returned by loadModel
     * @param prompt Input prompt text
     * @param maxTokens Maximum number of tokens to generate
     * @param temperature Sampling temperature (0.0 = greedy, higher = more random)
     * @param callback Callback invoked for each generated token (for streaming)
     * @return Complete generated text
     */
    external fun generateCompletion(
        modelHandle: Long,
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        callback: TokenCallback
    ): String

    /**
     * Generate embeddings for the given text.
     *
     * @param modelHandle Handle to a loaded embedding model
     * @param text Input text to embed
     * @return Float array of embedding values
     */
    external fun generateEmbedding(modelHandle: Long, text: String): FloatArray

    /**
     * Unload a model and free resources.
     *
     * @param modelHandle Handle to the loaded model
     */
    external fun unloadModel(modelHandle: Long)

    /**
     * Get current token generation speed in tokens per second.
     *
     * @param modelHandle Handle to the loaded model
     * @return Tokens per second, or 0 if not generating
     */
    external fun getTokensPerSecond(modelHandle: Long): Float

    /**
     * Callback interface for streaming token generation.
     */
    interface TokenCallback {
        /**
         * Called for each generated token.
         * @param token The newly generated token text
         * @return true to continue generation, false to stop
         */
        fun onToken(token: String): Boolean
    }
}
