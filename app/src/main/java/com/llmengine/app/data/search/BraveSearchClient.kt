package com.llmengine.app.data.search

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Client for the Brave Search API.
 *
 * IMPORTANT: No API key is stored in this source code.
 * Users must provide their own API key in the app settings.
 *
 * To obtain an API key:
 * 1. Visit https://brave.com/search/api/
 * 2. Create a free account
 * 3. Generate an API key
 * 4. Paste the key into the app settings
 */
class BraveSearchClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /**
     * Perform a web search using the Brave Search API.
     *
     * @param query Search query
     * @param apiKey User-provided Brave Search API key
     * @param count Number of results to return (max 20)
     * @return List of search results, or empty list on failure
     */
    suspend fun search(query: String, apiKey: String, count: Int = 5): List<SearchResult> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL?q=${java.net.URLEncoder.encode(query, "UTF-8")}&count=$count"

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .addHeader("Accept-Encoding", "gzip")
                    .addHeader("X-Subscription-Token", apiKey)
                    .get()
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext emptyList()
                }

                val body = response.body?.string() ?: return@withContext emptyList()
                val searchResponse = gson.fromJson(body, BraveSearchResponse::class.java)

                searchResponse.web?.results?.map {
                    SearchResult(
                        title = it.title ?: "",
                        url = it.url ?: "",
                        description = it.description ?: ""
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
        private const val BASE_URL = "https://api.search.brave.com/res/v1/web/search"
    }
}

data class SearchResult(
    val title: String,
    val url: String,
    val description: String
)

// Brave Search API response models
data class BraveSearchResponse(
    @SerializedName("web") val web: WebResults?
)

data class WebResults(
    @SerializedName("results") val results: List<WebResult>?
)

data class WebResult(
    @SerializedName("title") val title: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("description") val description: String?
)
