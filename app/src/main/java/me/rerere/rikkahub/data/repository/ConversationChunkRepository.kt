package me.rerere.rikkahub.data.repository

import android.util.Log
import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.ai.EmbeddingService
import me.rerere.rikkahub.data.db.dao.ConversationChunkDAO
import me.rerere.rikkahub.data.db.entity.ConversationChunkEntity
import me.rerere.rikkahub.data.db.entity.toByteArray
import kotlin.math.max

private const val TAG = "ConversationChunkRepo"

class ConversationChunkRepository(
    private val dao: ConversationChunkDAO
) {
    suspend fun insertChunk(chunk: ConversationChunkEntity): Long {
        return dao.insertChunk(chunk)
    }

    suspend fun updateEmbedding(id: Int, embedding: ByteArray) {
        dao.updateEmbedding(id, embedding)
    }

    suspend fun getChunksOfAssistant(assistantId: String): List<ConversationChunkEntity> {
        return dao.getChunksOfAssistant(assistantId)
    }

    suspend fun getChunkCountOfAssistant(assistantId: String): Int {
        return dao.getChunkCountOfAssistant(assistantId)
    }

    suspend fun getEmbeddedChunkCountOfAssistant(assistantId: String): Int {
        return dao.getEmbeddedChunkCountOfAssistant(assistantId)
    }

    suspend fun clearAllChunks(assistantId: String) {
        dao.clearAllChunks(assistantId)
    }

    suspend fun clearAllEmbeddings(assistantId: String) {
        dao.clearAllEmbeddings(assistantId)
    }

    suspend fun deleteAllChunks(assistantId: String) {
        dao.deleteAllChunks(assistantId)
    }

    suspend fun chunkAndStoreConversation(
        assistantId: String,
        messages: List<UIMessage>,
        chunkSize: Int,
        overlapPercent: Int,
        embeddingService: EmbeddingService,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ) {
        Log.d(TAG, "Starting chunkAndStoreConversation for \$assistantId")
        
        // 1. Clear existing
        dao.clearAllChunks(assistantId)
        
        // 2. Concatenate, tracking each message's start byte for later retain-gate filtering
        val messageStartBytes = mutableListOf<Pair<Int, Int>>() // (messageIdx, startByte)
        val fullTextBuilder = StringBuilder()
        for ((idx, msg) in messages.withIndex()) {
            val roleLabel = when (msg.role) {
                MessageRole.USER -> "User"
                MessageRole.ASSISTANT -> "Assistant"
                MessageRole.SYSTEM -> "System"
                else -> msg.role.name
            }
            val content = msg.parts.filterIsInstance<UIMessagePart.Text>().joinToString("") { it.text }
            if (content.isNotBlank()) {
                messageStartBytes.add(idx to fullTextBuilder.length)
                fullTextBuilder.append(roleLabel).append(": ").append(content).append("\n\n")
            }
        }
        val fullText = fullTextBuilder.toString()

        // 3. Chunk, tagging each chunk with the highest messageIdx it contains content from
        val chunks = mutableListOf<Pair<String, Int>>() // (content, messageEndIdx)
        val overlapChars = max(0, (chunkSize * overlapPercent) / 100)
        val step = max(1, chunkSize - overlapChars)

        var currentIndex = 0
        while (currentIndex < fullText.length) {
            val end = (currentIndex + chunkSize).coerceAtMost(fullText.length)
            val chunkContent = fullText.substring(currentIndex, end)
            val messageEndIdx = messageStartBytes.lastOrNull { it.second < end }?.first ?: 0
            chunks.add(chunkContent to messageEndIdx)
            if (end == fullText.length) break
            currentIndex += step
        }

        Log.d(TAG, "Created \${chunks.size} chunks. Storing and embedding...")

        // 4. Store and Embed
        val total = chunks.size
        for ((index, chunkPair) in chunks.withIndex()) {
            val (chunkContent, messageEndIdx) = chunkPair
            val entity = ConversationChunkEntity(
                assistantId = assistantId,
                content = chunkContent,
                chunkIndex = index,
                messageEndIdx = messageEndIdx
            )
            val id = dao.insertChunk(entity).toInt()

            try {
                val embedding = embeddingService.embed(chunkContent)
                if (embedding != null) {
                    dao.updateEmbedding(id, embedding.toByteArray())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to embed chunk \$index for \$assistantId", e)
            }

            onProgress(index + 1, total)
        }
        
        Log.d(TAG, "Finished chunkAndStoreConversation for \$assistantId")
    }
}
