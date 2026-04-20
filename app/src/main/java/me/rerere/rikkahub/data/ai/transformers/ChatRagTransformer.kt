package me.rerere.rikkahub.data.ai.transformers

import android.util.Log
import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.ai.EmbeddingService
import me.rerere.rikkahub.data.ai.searchSimilarChunks
import me.rerere.rikkahub.data.model.InjectionPosition
import me.rerere.rikkahub.data.model.PromptInjection
import me.rerere.rikkahub.data.repository.ConversationChunkRepository

private const val TAG = "ChatRagTransformer"

class ChatRagTransformer(
    private val embeddingService: EmbeddingService,
    private val conversationChunkRepository: ConversationChunkRepository
) : InputMessageTransformer {
    override suspend fun transform(
        ctx: TransformerContext,
        messages: List<UIMessage>
    ): List<UIMessage> {
        val assistant = ctx.assistant
        if (!assistant.ragEnabled) {
            return messages
        }

        // Get last user message text
        val lastUserMessage = messages.lastOrNull { it.role == MessageRole.USER }
        val lastUserText = lastUserMessage?.parts
            ?.filterIsInstance<UIMessagePart.Text>()
            ?.joinToString("\n") { it.text }

        if (lastUserText.isNullOrBlank()) {
            Log.d(TAG, "No valid last user message text found")
            return messages
        }

        if (!embeddingService.isConfigured) {
            Log.w(TAG, "Embedding service not configured, skipping RAG")
            return messages
        }

        // Embed the query
        val queryEmbedding = try {
            embeddingService.embed(lastUserText)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to embed last user message for RAG", e)
            null
        }

        if (queryEmbedding == null) {
            return messages
        }

        // Search similar chunks
        val assistantId = assistant.id.toString()
        val allChunks = try {
            conversationChunkRepository.getChunksOfAssistant(assistantId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch chunks", e)
            return messages
        }

        // Retain gate: exclude chunks whose content overlaps the most recently vectorized messages.
        // Cutoff is derived from chunks' own max messageEndIdx (the conversation length at vectorize time),
        // NOT from messages.size — that would conflate context-window size with conversation length and
        // silently kill RAG whenever contextMessageSize is small (A/B test failure mode).
        val maxChunkEndIdx = allChunks.maxOfOrNull { it.messageEndIdx } ?: -1
        val cutoff = maxChunkEndIdx - assistant.ragSkipRecentMessages + 1
        if (cutoff <= 0) {
            Log.d(TAG, "Vectorized conversation shorter than retain window, skipping RAG (maxEndIdx=$maxChunkEndIdx, skip=${assistant.ragSkipRecentMessages})")
            return messages
        }

        val similarChunks = searchSimilarChunks(
            queryEmbedding = queryEmbedding,
            chunks = allChunks,
            topK = assistant.ragTopK,
            threshold = assistant.ragThreshold,
            messageEndIdxMax = cutoff
        )

        if (similarChunks.isEmpty()) {
            Log.d(TAG, "No similar chunks found for RAG")
            return messages
        }

        Log.d(TAG, "Found \${similarChunks.size} similar chunks")

        // Format RAG content
        val joinedChunks = similarChunks.joinToString("\n---\n") { it.chunk.content }
        val formattedRagText = assistant.ragTemplate.replace("{{text}}", joinedChunks)

        // Create injection
        val ragInjection = PromptInjection.ModeInjection(
            name = "RAG",
            enabled = true,
            priority = 50,
            position = assistant.ragPosition,
            content = formattedRagText,
            injectDepth = assistant.ragDepth,
            role = assistant.ragRole
        )

        val byPosition = mapOf(ragInjection.position to listOf(ragInjection))

        return applyInjections(messages, byPosition)
    }
}
