package com.llmengine.app.data.search

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Client for the Tavily Search API.
 *
 * IMPORTANT: No API key is stored in this source code.
 * Users must provide their own API key in the app settings.
 *
 * To obtain an API key:
 * 1. Visit https://app.tavily.com
 * 2. Create a free account (1,000 free credits/month)
 * 3. Generate an API key
 * 4. Paste the key into the app settings
 */
class TavilySearchClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Perform a web search using the Tavily Search API.
     *
     * @param query Search query
     * @param apiKey User-provided Tavily API key
     * @param maxResults Number of results to return (max 20)
     * @return List of search results, or empty list on failure
     */
    suspend fun search(query: String, apiKey: String, maxResults: Int = 5): List<SearchResult> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = gson.toJson(
                    TavilySearchRequest(
                        apiKey = apiKey,
                        query = query,
                        maxResults = maxResults
                    )
                )

                val request = Request.Builder()
                    .url(BASE_URL)
                    .post(requestBody.toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext emptyList()
                }

                val body = response.body?.string() ?: return@withContext emptyList()
                val searchResponse = gson.fromJson(body, TavilySearchResponse::class.java)

                searchResponse.results?.map {
                    SearchResult(
                        title = it.title ?: "",
                        url = it.url ?: "",
                        description = it.content ?: ""
                    )
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Format search results as context for the LLM prompt.
     */
    fun formatResultsAsContext(results: List<SearchResult>): String {
        if (results.isEmpty()) return ""

        return results.mapIndexed { index, result ->
            "[${index + 1}] ${result.title}\n${result.url}\n${result.description}"
        }.joinToString("\n\n")
    }

    companion object {
        private const val BASE_URL = "https://api.tavily.com/search"
    }
}

// Tavily Search API request model
data class TavilySearchRequest(
    @SerializedName("api_key") val apiKey: String,
    @SerializedName("query") val query: String,
    @SerializedName("max_results") val maxResults: Int
)

// Tavily Search API response models
data class TavilySearchResponse(
    @SerializedName("results") val results: List<TavilyResult>?
)

data class TavilyResult(
    @SerializedName("title") val title: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("content") val content: String?
)
