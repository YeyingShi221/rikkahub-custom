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

private fun splitRecursive(
    input: String,
    maxLength: Int,
    delimiters: List<String> = listOf("\n\n", "\n", " ", "")
): List<String> {
    if (maxLength <= 0 || input.length <= maxLength) return listOf(input)
    val delim = delimiters.firstOrNull() ?: ""
    val parts = if (delim.isEmpty()) {
        input.chunked(maxLength)
    } else {
        input.split(delim)
    }
    val flat = parts.flatMap { p ->
        if (p.length <= maxLength) listOf(p)
        else splitRecursive(p, maxLength, delimiters.drop(1))
    }
    val result = mutableListOf<String>()
    var current = StringBuilder()
    for (part in flat) {
        if (current.isEmpty()) {
            current.append(part)
        } else if (current.length + delim.length + part.length <= maxLength) {
            current.append(delim).append(part)
        } else {
            result.add(current.toString())
            current = StringBuilder(part)
        }
    }
    if (current.isNotEmpty()) result.add(current.toString())
    return result
}

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

    data class ChunkDiffResult(
        val totalChunks: Int,
        val newChunks: Int,
        val staleChunks: Int,
        val firstChangeDepth: Int,
        val needsConfirmation: Boolean
    )

    suspend fun previewChanges(
        assistantId: String,
        messages: List<UIMessage>,
        chunkSize: Int,
        overlapPercent: Int,
        depthThreshold: Int = 30
    ): ChunkDiffResult {
        val fullText = buildFullText(messages)
        val chunks = buildChunks(fullText, chunkSize, overlapPercent, messages)
        val existingHashes = dao.getEmbeddedHashesOfAssistant(assistantId).toSet()
        val newHashes = chunks.map { it.first.hashCode() }.toSet()
        val stale = existingHashes - newHashes
        val fresh = newHashes - existingHashes

        val firstChangeIdx = if (fresh.isEmpty()) chunks.size else {
            chunks.indexOfFirst { it.first.hashCode() in fresh }
        }
        val depth = chunks.size - firstChangeIdx

        return ChunkDiffResult(
            totalChunks = chunks.size,
            newChunks = fresh.size,
            staleChunks = stale.size,
            firstChangeDepth = depth,
            needsConfirmation = depth >= depthThreshold && stale.isNotEmpty()
        )
    }

    private fun buildFullText(messages: List<UIMessage>): String {
        val builder = StringBuilder()
        for (msg in messages) {
            val roleLabel = when (msg.role) {
                MessageRole.USER -> "User"
                MessageRole.ASSISTANT -> "Assistant"
                MessageRole.SYSTEM -> "System"
                else -> msg.role.name
            }
            val content = msg.parts.filterIsInstance<UIMessagePart.Text>().joinToString("") { it.text }
            if (content.isNotBlank()) {
                builder.append(roleLabel).append(": ").append(content).append("\n\n")
            }
        }
        return builder.toString()
    }

    private fun buildChunks(
        fullText: String,
        chunkSize: Int,
        overlapPercent: Int,
        messages: List<UIMessage>
    ): List<Pair<String, Int>> {
        val messageStartBytes = mutableListOf<Pair<Int, Int>>()
        var pos = 0
        for ((idx, msg) in messages.withIndex()) {
            val content = msg.parts.filterIsInstance<UIMessagePart.Text>().joinToString("") { it.text }
            if (content.isNotBlank()) {
                messageStartBytes.add(idx to pos)
                pos += content.length + msg.role.name.length + 4
            }
        }

        val overlapChars = max(0, (chunkSize * overlapPercent) / 100)
        val softMax = chunkSize
        val hardMax = (softMax * 1.3).toInt()
        val rawParts = splitRecursive(fullText, softMax, listOf("\n\n", "\n", " ", ""))

        val mergedParts = mutableListOf<String>()
        var current = StringBuilder()
        for (part in rawParts) {
            if (current.isEmpty()) {
                current.append(part)
            } else if (current.length + part.length + 2 <= hardMax) {
                current.append("\n\n").append(part)
            } else {
                mergedParts.add(current.toString())
                if (overlapChars > 0 && current.length > overlapChars) {
                    var snapIdx = current.length - overlapChars
                    while (snapIdx < current.length && current[snapIdx] != ' ' && current[snapIdx] != '\n') snapIdx++
                    val overlap = if (snapIdx < current.length) current.substring(snapIdx).trimStart() else ""
                    current = StringBuilder(overlap).append("\n\n").append(part)
                } else {
                    current = StringBuilder(part)
                }
            }
        }
        if (current.isNotEmpty()) mergedParts.add(current.toString())

        val chunks = mutableListOf<Pair<String, Int>>()
        var searchFrom = 0
        for (chunkContent in mergedParts) {
            searchFrom += chunkContent.length
            val endByte = searchFrom.coerceAtMost(fullText.length)
            val messageEndIdx = messageStartBytes.lastOrNull { it.second < endByte }?.first ?: 0
            chunks.add(chunkContent to messageEndIdx)
        }
        return chunks
    }

    suspend fun chunkAndStoreConversation(
        assistantId: String,
        messages: List<UIMessage>,
        chunkSize: Int,
        overlapPercent: Int,
        embeddingService: EmbeddingService,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ) {
        Log.d(TAG, "Starting chunkAndStoreConversation for $assistantId")

        val fullText = buildFullText(messages)
        val chunks = buildChunks(fullText, chunkSize, overlapPercent, messages)
        Log.d(TAG, "Created ${chunks.size} chunks")

        // 4. Diff: compare new chunk hashes vs existing, only process delta
        val existingHashes = dao.getEmbeddedHashesOfAssistant(assistantId).toSet()
        val newChunkHashes = chunks.map { it.first.hashCode() }.toSet()

        // Delete chunks whose hashes disappeared (swipe/edit changed them)
        val stalledHashes = existingHashes - newChunkHashes
        if (stalledHashes.isNotEmpty()) {
            for (staleHash in stalledHashes) {
                dao.deleteByHash(assistantId, staleHash)
            }
            Log.d(TAG, "Deleted ${stalledHashes.size} stale chunks")
        }

        // Only embed chunks whose hashes are new
        val toEmbed = chunks.filter { it.first.hashCode() !in existingHashes }
        var newCount = 0
        val total = chunks.size
        for ((index, chunkPair) in chunks.withIndex()) {
            val (chunkContent, messageEndIdx) = chunkPair
            val hash = chunkContent.hashCode()

            if (hash in existingHashes) {
                onProgress(index + 1, total)
                continue
            }

            val entity = ConversationChunkEntity(
                assistantId = assistantId,
                content = chunkContent,
                contentHash = hash,
                chunkIndex = index,
                messageEndIdx = messageEndIdx
            )
            val id = dao.insertChunk(entity).toInt()

            try {
                val embedding = embeddingService.embed(chunkContent)
                if (embedding != null) {
                    dao.updateEmbedding(id, embedding.toByteArray())
                    newCount++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to embed chunk $index for $assistantId", e)
            }

            onProgress(index + 1, total)
        }
        Log.d(TAG, "Diff: ${stalledHashes.size} deleted, $newCount new, ${existingHashes.size - stalledHashes.size} kept")

        Log.d(TAG, "Finished chunkAndStoreConversation for $assistantId")
    }
}
