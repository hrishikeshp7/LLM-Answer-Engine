package com.llmengine.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llmengine.app.LLMEngineApp
import com.llmengine.app.data.model.AppSettings
import com.llmengine.app.data.model.ChatMessage
import com.llmengine.app.data.model.SearchSource
import com.llmengine.app.data.preferences.SecurePreferences
import com.llmengine.app.data.search.BraveSearchClient
import com.llmengine.app.download.ModelDownloadManager
import com.llmengine.app.inference.InferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the chat screen.
 * Manages conversation state, model inference, and optional web search.
 */
class ChatViewModel : ViewModel() {

    private val context = LLMEngineApp.instance
    private val prefs = SecurePreferences(context)
    private val downloadManager = ModelDownloadManager(context)
    private val inferenceManager = InferenceManager()
    private val searchClient = BraveSearchClient()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _currentResponse = MutableStateFlow("")
    val currentResponse: StateFlow<String> = _currentResponse.asStateFlow()

    private val _tokensPerSecond = MutableStateFlow(0f)
    val tokensPerSecond: StateFlow<Float> = _tokensPerSecond.asStateFlow()

    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()

    private var settings: AppSettings = prefs.loadSettings()

    init {
        loadModel()
    }

    private fun loadModel() {
        viewModelScope.launch {
            val modelPath = downloadManager.getModelFilePath("qwen2.5-0.5b")
            val loaded = inferenceManager.loadModel(modelPath, settings)
            _isModelLoaded.value = loaded
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank() || _isGenerating.value) return

        val userMessage = ChatMessage(
            content = userText,
            isUser = true
        )
        _messages.value = _messages.value + userMessage
        _isGenerating.value = true
        _currentResponse.value = ""

        viewModelScope.launch {
            try {
                var searchContext: String? = null
                var sources: List<SearchSource> = emptyList()

                // Perform web search if enabled and API key is available
                if (settings.webSearchEnabled && prefs.hasBraveApiKey()) {
                    val apiKey = prefs.getBraveApiKey()!!
                    val results = searchClient.search(userText, apiKey)
                    if (results.isNotEmpty()) {
                        searchContext = searchClient.formatResultsAsContext(results)
                        sources = results.map { result ->
                            SearchSource(
                                title = result.title,
                                url = result.url,
                                snippet = result.description
                            )
                        }
                    }
                }

                // Build conversation history for context
                val history = _messages.value
                    .dropLast(1) // Exclude the message we just added
                    .windowed(2, 2, partialWindows = false)
                    .mapNotNull { pair ->
                        if (pair[0].isUser && !pair[1].isUser) {
                            Pair(pair[0].content, pair[1].content)
                        } else null
                    }

                val prompt = InferenceManager.formatPrompt(
                    systemMessage = "You are a helpful AI assistant running locally on an Android device. Be concise and accurate.",
                    conversationHistory = history,
                    userMessage = userText,
                    searchContext = searchContext
                )

                val responseBuilder = StringBuilder()

                inferenceManager.generateStream(prompt, settings).collect { token ->
                    responseBuilder.append(token)
                    _currentResponse.value = responseBuilder.toString()
                    _tokensPerSecond.value = inferenceManager.getTokensPerSecond()
                }

                val assistantMessage = ChatMessage(
                    content = responseBuilder.toString(),
                    isUser = false,
                    sources = sources
                )
                _messages.value = _messages.value + assistantMessage

            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    content = "Error: ${e.message ?: "Unknown error occurred"}",
                    isUser = false
                )
                _messages.value = _messages.value + errorMessage
            } finally {
                _isGenerating.value = false
                _currentResponse.value = ""
            }
        }
    }

    fun refreshSettings() {
        settings = prefs.loadSettings()
    }

    fun clearConversation() {
        _messages.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        inferenceManager.unload()
    }
}
