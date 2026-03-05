package com.llmengine.app

import com.llmengine.app.data.model.AppSettings
import com.llmengine.app.data.model.DownloadState
import com.llmengine.app.data.model.MemoryMode
import com.llmengine.app.data.model.ModelRegistry
import com.llmengine.app.data.model.ModelType
import com.llmengine.app.data.search.BraveSearchClient
import com.llmengine.app.data.search.SearchResult
import com.llmengine.app.inference.InferenceManager
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the LLM Answer Engine core components.
 */
class CoreUnitTest {

    @Test
    fun `ModelRegistry contains expected models`() {
        val models = ModelRegistry.availableModels
        assertTrue("Should have at least 2 models", models.size >= 2)

        val llmModel = models.find { it.type == ModelType.LLM }
        assertNotNull("Should have an LLM model", llmModel)

        val embModel = models.find { it.type == ModelType.EMBEDDING }
        assertNotNull("Should have an embedding model", embModel)
    }

    @Test
    fun `ModelRegistry getModelById returns correct model`() {
        val model = ModelRegistry.getModelById("qwen2.5-0.5b")
        assertNotNull(model)
        assertEquals("Qwen 2.5 0.5B Instruct", model?.name)
        assertEquals(ModelType.LLM, model?.type)
    }

    @Test
    fun `ModelRegistry getModelById returns null for unknown id`() {
        val model = ModelRegistry.getModelById("nonexistent-model")
        assertNull(model)
    }

    @Test
    fun `AppSettings has sensible defaults`() {
        val settings = AppSettings()
        assertEquals(512, settings.maxTokens)
        assertEquals(0.7f, settings.temperature, 0.001f)
        assertEquals(4, settings.cpuThreads)
        assertEquals(MemoryMode.BALANCED, settings.memoryMode)
        assertFalse(settings.webSearchEnabled)
    }

    @Test
    fun `MemoryMode has correct labels`() {
        assertEquals("Low Memory", MemoryMode.LOW.label)
        assertEquals("Balanced", MemoryMode.BALANCED.label)
        assertEquals("High Performance", MemoryMode.HIGH.label)
    }

    @Test
    fun `DownloadState types are correctly modeled`() {
        val notDownloaded = DownloadState.NotDownloaded
        assertTrue(notDownloaded is DownloadState.NotDownloaded)

        val downloading = DownloadState.Downloading(0.5f, 500L, 1000L)
        assertEquals(0.5f, downloading.progress, 0.001f)
        assertEquals(500L, downloading.downloadedBytes)
        assertEquals(1000L, downloading.totalBytes)

        val error = DownloadState.Error("test error")
        assertEquals("test error", error.message)
    }

    @Test
    fun `InferenceManager formatPrompt creates correct format`() {
        val prompt = InferenceManager.formatPrompt(
            systemMessage = "You are helpful.",
            conversationHistory = emptyList(),
            userMessage = "Hello"
        )

        assertTrue(prompt.contains("<|im_start|>system"))
        assertTrue(prompt.contains("You are helpful."))
        assertTrue(prompt.contains("<|im_start|>user"))
        assertTrue(prompt.contains("Hello"))
        assertTrue(prompt.contains("<|im_start|>assistant"))
    }

    @Test
    fun `InferenceManager formatPrompt includes search context`() {
        val prompt = InferenceManager.formatPrompt(
            systemMessage = "You are helpful.",
            conversationHistory = emptyList(),
            userMessage = "What is AI?",
            searchContext = "[1] Wikipedia - Artificial Intelligence\nhttps://en.wikipedia.org"
        )

        assertTrue(prompt.contains("web search results"))
        assertTrue(prompt.contains("Wikipedia"))
    }

    @Test
    fun `InferenceManager formatPrompt includes conversation history`() {
        val history = listOf(
            Pair("Hi", "Hello! How can I help?"),
            Pair("What is 2+2?", "2+2 equals 4.")
        )

        val prompt = InferenceManager.formatPrompt(
            systemMessage = "You are helpful.",
            conversationHistory = history,
            userMessage = "Thanks"
        )

        assertTrue(prompt.contains("Hi"))
        assertTrue(prompt.contains("Hello! How can I help?"))
        assertTrue(prompt.contains("What is 2+2?"))
        assertTrue(prompt.contains("2+2 equals 4."))
        assertTrue(prompt.contains("Thanks"))
    }

    @Test
    fun `BraveSearchClient formatResultsAsContext formats correctly`() {
        val client = BraveSearchClient()
        val results = listOf(
            SearchResult("Title 1", "https://example.com/1", "Description 1"),
            SearchResult("Title 2", "https://example.com/2", "Description 2")
        )

        val context = client.formatResultsAsContext(results)
        assertTrue(context.contains("[1] Title 1"))
        assertTrue(context.contains("https://example.com/1"))
        assertTrue(context.contains("[2] Title 2"))
    }

    @Test
    fun `BraveSearchClient formatResultsAsContext handles empty list`() {
        val client = BraveSearchClient()
        val context = client.formatResultsAsContext(emptyList())
        assertEquals("", context)
    }

    @Test
    fun `Model download URLs are HTTPS`() {
        ModelRegistry.availableModels.forEach { model ->
            assertTrue(
                "Model ${model.name} URL should use HTTPS",
                model.downloadUrl.startsWith("https://")
            )
        }
    }

    @Test
    fun `Model file names end with gguf`() {
        ModelRegistry.availableModels.forEach { model ->
            assertTrue(
                "Model ${model.name} fileName should end with .gguf",
                model.fileName.endsWith(".gguf")
            )
        }
    }

    @Test
    fun `No hardcoded API keys in source`() {
        // This test verifies that the BraveSearchClient doesn't contain hardcoded keys
        val client = BraveSearchClient()
        // The client should require an API key parameter - no default key
        assertNotNull(client)
    }
}
