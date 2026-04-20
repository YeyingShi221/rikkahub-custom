package me.rerere.rikkahub.data.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

private const val TAG = "EmbeddingService"

/**
 * Service for Google Gemini Embedding API
 *
 * @param apiKey Google Gemini API Key
 */
class EmbeddingService(private val apiKeyProvider: () -> String) {
    private val apiKey: String get() = apiKeyProvider()
    val isConfigured: Boolean get() = apiKey.isNotBlank()
    private val client = OkHttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    /**
     * Convert text into a 3072-dimensional float vector using gemini-embedding-2-preview model.
     *
     * @param text The input text to embed
     * @return FloatArray(3072) on success, null on failure
     */
    suspend fun embed(text: String): FloatArray? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext null
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-2-preview:embedContent?key=$apiKey"
        
        val requestBody = EmbeddingRequest(
            model = "models/gemini-embedding-2-preview",
            content = EmbeddingContent(
                parts = listOf(EmbeddingPart(text = text))
            )
        )
        
        val bodyString = json.encodeToString(EmbeddingRequest.serializer(), requestBody)
        val request = Request.Builder()
            .url(url)
            .post(bodyString.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "API request failed with code: ${response.code}, message: ${response.message}")
                    Log.e(TAG, "Error body: ${response.body?.string()}")
                    return@withContext null
                }

                val responseBody = response.body?.string() ?: return@withContext null
                val embeddingResponse = json.decodeFromString(EmbeddingResponse.serializer(), responseBody)
                
                val values = embeddingResponse.embedding.values
                if (values.size != 3072) {
                    Log.w(TAG, "Expected 3072 dimensions, but got ${values.size}")
                }
                
                return@withContext values
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error during embedding: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during embedding: ${e.message}", e)
            null
        }
    }

    @Serializable
    private data class EmbeddingRequest(
        val model: String,
        val content: EmbeddingContent
    )

    @Serializable
    private data class EmbeddingContent(
        val parts: List<EmbeddingPart>
    )

    @Serializable
    private data class EmbeddingPart(
        val text: String
    )

    @Serializable
    private data class EmbeddingResponse(
        val embedding: EmbeddingValues
    )

    @Serializable
    private data class EmbeddingValues(
        val values: FloatArray
    )
}
