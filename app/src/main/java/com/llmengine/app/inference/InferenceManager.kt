package com.llmengine.app.inference

import com.llmengine.app.data.model.AppSettings
import com.llmengine.app.data.model.MemoryMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * High-level inference manager that handles model loading and text generation.
 * Wraps the JNI LlamaInference layer with a Kotlin-friendly API.
 */
class InferenceManager {

    private val llama = LlamaInference()
    private var modelHandle: Long = -1L
    private var embeddingHandle: Long = -1L
    private var isNativeAvailable = false

    init {
        isNativeAvailable = try {
            // Test if native library is loaded
            System.loadLibrary("llama_jni")
            true
        } catch (_: UnsatisfiedLinkError) {
            false
        }
    }

    suspend fun loadModel(modelPath: String, settings: AppSettings): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!isNativeAvailable) {
                    // Return true in stub mode for development
                    return@withContext true
                }
                val contextSize = when (settings.memoryMode) {
                    MemoryMode.LOW -> 512
                    MemoryMode.BALANCED -> 2048
                    MemoryMode.HIGH -> 4096
                }
                modelHandle = llama.loadModel(modelPath, settings.cpuThreads, contextSize)
                modelHandle != -1L
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun loadEmbeddingModel(modelPath: String, settings: AppSettings): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!isNativeAvailable) return@withContext true
                embeddingHandle = llama.loadModel(modelPath, settings.cpuThreads, 512)
                embeddingHandle != -1L
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Generate a streaming response for the given prompt.
     * Emits tokens one at a time for display.
     */
    fun generateStream(prompt: String, settings: AppSettings): Flow<String> = flow {
        if (!isNativeAvailable) {
            // Stub mode: simulate streaming response
            val stubResponse = "This is a simulated response. " +
                "The native inference library is not loaded. " +
                "To enable real inference, build the native library with the Android NDK " +
                "and include a GGUF model file."
            for (word in stubResponse.split(" ")) {
                emit("$word ")
                kotlinx.coroutines.delay(50)
            }
            return@flow
        }

        val fullResponse = llama.generateCompletion(
            modelHandle,
            prompt,
            settings.maxTokens,
            settings.temperature,
            object : LlamaInference.TokenCallback {
                override fun onToken(token: String): Boolean {
                    return true
                }
            }
        )
        emit(fullResponse)
    }.flowOn(Dispatchers.IO)

    suspend fun generateEmbedding(text: String): FloatArray {
        return withContext(Dispatchers.IO) {
            if (!isNativeAvailable) {
                // Return a stub embedding
                FloatArray(384) { 0f }
            } else {
                llama.generateEmbedding(embeddingHandle, text)
            }
        }
    }

    fun getTokensPerSecond(): Float {
        if (!isNativeAvailable) return 0f
        return llama.getTokensPerSecond(modelHandle)
    }

    fun unload() {
        if (isNativeAvailable) {
            if (modelHandle != -1L) {
                llama.unloadModel(modelHandle)
                modelHandle = -1L
            }
            if (embeddingHandle != -1L) {
                llama.unloadModel(embeddingHandle)
                embeddingHandle = -1L
            }
        }
    }

    fun isModelLoaded(): Boolean = modelHandle != -1L || !isNativeAvailable

    companion object {
        /**
         * Formats a chat prompt with system message and conversation history.
         */
        fun formatPrompt(
            systemMessage: String,
            conversationHistory: List<Pair<String, String>>,
            userMessage: String,
            searchContext: String? = null
        ): String {
            val sb = StringBuilder()
            sb.appendLine("<|im_start|>system")
            sb.appendLine(systemMessage)
            if (!searchContext.isNullOrBlank()) {
                sb.appendLine()
                sb.appendLine("Relevant web search results:")
                sb.appendLine(searchContext)
                sb.appendLine("Use these results to provide accurate, up-to-date information. Cite sources when relevant.")
            }
            sb.appendLine("<|im_end|>")

            for ((user, assistant) in conversationHistory) {
                sb.appendLine("<|im_start|>user")
                sb.appendLine(user)
                sb.appendLine("<|im_end|>")
                sb.appendLine("<|im_start|>assistant")
                sb.appendLine(assistant)
                sb.appendLine("<|im_end|>")
            }

            sb.appendLine("<|im_start|>user")
            sb.appendLine(userMessage)
            sb.appendLine("<|im_end|>")
            sb.appendLine("<|im_start|>assistant")
            return sb.toString()
        }
    }
}
