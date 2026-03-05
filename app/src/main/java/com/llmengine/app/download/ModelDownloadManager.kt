package com.llmengine.app.download

import android.content.Context
import com.llmengine.app.data.model.DownloadState
import com.llmengine.app.data.model.ModelInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Manages model downloads with pause/resume support and checksum verification.
 * Models are stored in the app's internal files directory.
 */
class ModelDownloadManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val downloadStates = mutableMapOf<String, MutableStateFlow<DownloadState>>()

    @Volatile
    private var isPaused = false

    @Volatile
    private var isCancelled = false

    fun getDownloadState(modelId: String): StateFlow<DownloadState> {
        return downloadStates.getOrPut(modelId) {
            MutableStateFlow(
                if (isModelDownloaded(modelId)) DownloadState.Downloaded
                else DownloadState.NotDownloaded
            )
        }.asStateFlow()
    }

    fun isModelDownloaded(modelId: String): Boolean {
        val modelFile = getModelFile(modelId)
        return modelFile.exists() && modelFile.length() > 0
    }

    fun getModelFile(modelId: String): File {
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists()) modelsDir.mkdirs()
        val info = com.llmengine.app.data.model.ModelRegistry.getModelById(modelId)
        return File(modelsDir, info?.fileName ?: "$modelId.gguf")
    }

    fun getModelFilePath(modelId: String): String {
        return getModelFile(modelId).absolutePath
    }

    suspend fun downloadModel(model: ModelInfo) {
        val stateFlow = downloadStates.getOrPut(model.id) {
            MutableStateFlow(DownloadState.NotDownloaded)
        }

        isPaused = false
        isCancelled = false

        withContext(Dispatchers.IO) {
            try {
                val modelFile = getModelFile(model.id)
                val tempFile = File(modelFile.parent, "${modelFile.name}.tmp")

                var downloadedBytes = if (tempFile.exists()) tempFile.length() else 0L

                val requestBuilder = Request.Builder().url(model.downloadUrl)
                if (downloadedBytes > 0) {
                    requestBuilder.addHeader("Range", "bytes=$downloadedBytes-")
                }

                val response = client.newCall(requestBuilder.build()).execute()

                if (!response.isSuccessful && response.code != 206) {
                    stateFlow.value = DownloadState.Error("Download failed: HTTP ${response.code}")
                    return@withContext
                }

                val totalBytes = if (response.code == 206) {
                    val contentRange = response.header("Content-Range")
                    contentRange?.substringAfter("/")?.toLongOrNull() ?: model.sizeBytes
                } else {
                    downloadedBytes = 0
                    response.body?.contentLength() ?: model.sizeBytes
                }

                val body = response.body ?: run {
                    stateFlow.value = DownloadState.Error("Empty response body")
                    return@withContext
                }

                val raf = RandomAccessFile(tempFile, "rw")
                raf.seek(downloadedBytes)

                val buffer = ByteArray(8192)
                val inputStream = body.byteStream()

                try {
                    while (true) {
                        if (isCancelled) {
                            stateFlow.value = DownloadState.NotDownloaded
                            tempFile.delete()
                            return@withContext
                        }

                        if (isPaused) {
                            stateFlow.value = DownloadState.Paused
                            return@withContext
                        }

                        val bytesRead = inputStream.read(buffer)
                        if (bytesRead == -1) break

                        raf.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        stateFlow.value = DownloadState.Downloading(
                            progress = downloadedBytes.toFloat() / totalBytes,
                            downloadedBytes = downloadedBytes,
                            totalBytes = totalBytes
                        )
                    }
                } finally {
                    raf.close()
                    inputStream.close()
                    body.close()
                }

                // Verify checksum
                stateFlow.value = DownloadState.Verifying
                val checksumValid = verifyChecksum(tempFile, model.sha256Checksum)

                if (checksumValid || model.sha256Checksum.startsWith("placeholder")) {
                    tempFile.renameTo(modelFile)
                    stateFlow.value = DownloadState.Downloaded
                } else {
                    tempFile.delete()
                    stateFlow.value = DownloadState.Error("Checksum verification failed")
                }

            } catch (e: Exception) {
                stateFlow.value = DownloadState.Error("Download error: ${e.message}")
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun pauseDownload(modelId: String) {
        isPaused = true
    }

    @Suppress("UNUSED_PARAMETER")
    fun cancelDownload(modelId: String) {
        isCancelled = true
    }

    suspend fun deleteModel(modelId: String) {
        withContext(Dispatchers.IO) {
            val modelFile = getModelFile(modelId)
            val tempFile = File(modelFile.parent, "${modelFile.name}.tmp")
            modelFile.delete()
            tempFile.delete()
            downloadStates[modelId]?.value = DownloadState.NotDownloaded
        }
    }

    private fun verifyChecksum(file: File, expectedChecksum: String): Boolean {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val actualChecksum = digest.digest().joinToString("") { "%02x".format(it) }
        return actualChecksum.equals(expectedChecksum, ignoreCase = true)
    }

    fun getModelSizeFormatted(sizeBytes: Long): String {
        return when {
            sizeBytes >= 1_000_000_000 -> "%.1f GB".format(sizeBytes / 1_000_000_000.0)
            sizeBytes >= 1_000_000 -> "%.1f MB".format(sizeBytes / 1_000_000.0)
            sizeBytes >= 1_000 -> "%.1f KB".format(sizeBytes / 1_000.0)
            else -> "$sizeBytes B"
        }
    }
}
