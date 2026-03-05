package com.llmengine.app.data.model

/**
 * Represents a downloadable model with metadata.
 */
data class ModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val fileName: String,
    val downloadUrl: String,
    val sizeBytes: Long,
    val sha256Checksum: String,
    val type: ModelType
)

enum class ModelType {
    LLM,
    EMBEDDING
}

/**
 * Represents the download state of a model.
 */
sealed class DownloadState {
    data object NotDownloaded : DownloadState()
    data class Downloading(val progress: Float, val downloadedBytes: Long, val totalBytes: Long) : DownloadState()
    data object Paused : DownloadState()
    data object Verifying : DownloadState()
    data object Downloaded : DownloadState()
    data class Error(val message: String) : DownloadState()
}

/**
 * Represents a chat message.
 */
data class ChatMessage(
    val id: Long = 0,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val sources: List<SearchSource> = emptyList()
)

/**
 * Represents a web search source used in RAG.
 */
data class SearchSource(
    val title: String,
    val url: String,
    val snippet: String
)

/**
 * Application settings stored locally.
 */
data class AppSettings(
    val maxTokens: Int = 512,
    val temperature: Float = 0.7f,
    val cpuThreads: Int = 4,
    val memoryMode: MemoryMode = MemoryMode.BALANCED,
    val webSearchEnabled: Boolean = false
)

enum class MemoryMode(val label: String) {
    LOW("Low Memory"),
    BALANCED("Balanced"),
    HIGH("High Performance")
}
