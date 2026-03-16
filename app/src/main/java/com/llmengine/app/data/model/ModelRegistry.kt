package com.llmengine.app.data.model

/**
 * Registry of available models that can be downloaded.
 * Models are NOT bundled in the app - they must be downloaded by the user.
 */
object ModelRegistry {
    /**
     * Available models for download.
     * URLs point to HuggingFace-hosted GGUF quantized models.
     */
    val availableModels = listOf(
        ModelInfo(
            id = "qwen2.5-0.5b",
            name = "Qwen 2.5 0.5B Instruct",
            description = "Small but capable language model for general chat and reasoning. Quantized to Q4_K_M for efficient on-device inference.",
            fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
            sizeBytes = 397_000_000L,
            sha256Checksum = "placeholder_checksum_update_after_verification",
            type = ModelType.LLM
        ),
        ModelInfo(
            id = "qwen1.5-0.5b",
            name = "Qwen 1.5 0.5B Chat",
            description = "Previous generation small model. Included as an alternative.",
            fileName = "qwen1_5-0_5b-chat-q4_k_m.gguf",
            downloadUrl = "https://huggingface.co/Qwen/Qwen1.5-0.5B-Chat-GGUF/resolve/main/qwen1_5-0_5b-chat-q4_k_m.gguf",
            sizeBytes = 390_000_000L,
            sha256Checksum = "placeholder",
            type = ModelType.LLM
        ),
        ModelInfo(
            id = "qwen2.5-0.8b",
            name = "Qwen 2.5 0.8B",
            description = "Slightly larger but very capable model. Needs more memory.",
            fileName = "qwen2.5-0.8b-q4_k_m.gguf",
            downloadUrl = "https://huggingface.co/second-state/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/Qwen2.5-0.5B-Instruct-Q4_K_M.gguf",
            sizeBytes = 400_000_000L,
            sha256Checksum = "placeholder",
            type = ModelType.LLM
        ),
        ModelInfo(
            id = "all-minilm-l6-v2",
            name = "All-MiniLM-L6-v2",
            description = "Small embedding model for semantic search and RAG. Converts text into vector representations.",
            fileName = "all-minilm-l6-v2-q4_k_m.gguf",
            downloadUrl = "https://huggingface.co/leliuga/all-MiniLM-L6-v2-GGUF/resolve/main/all-MiniLM-L6-v2.Q4_K_M.gguf",
            sizeBytes = 23_000_000L,
            sha256Checksum = "placeholder_checksum_update_after_verification",
            type = ModelType.EMBEDDING
        )
    )

    fun getModelById(id: String): ModelInfo? = availableModels.find { it.id == id }
}
