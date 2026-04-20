package me.rerere.rikkahub.data.ai

import me.rerere.rikkahub.data.db.entity.ConversationChunkEntity
import me.rerere.rikkahub.data.db.entity.MemoryEntity
import me.rerere.rikkahub.data.db.entity.toFloatArray

data class ScoredMemory(
    val memory: MemoryEntity,
    val score: Float
)

data class ScoredChunk(
    val chunk: ConversationChunkEntity,
    val score: Float
)

fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
    require(a.size == b.size) { "Vectors must be same size: ${a.size} vs ${b.size}" }
    var dotProduct = 0f
    var normA = 0f
    var normB = 0f
    for (i in a.indices) {
        dotProduct += a[i] * b[i]
        normA += a[i] * a[i]
        normB += b[i] * b[i]
    }
    val denominator = Math.sqrt(normA.toDouble()) * Math.sqrt(normB.toDouble())
    return if (denominator == 0.0) 0f else (dotProduct / denominator).toFloat()
}

fun searchSimilarMemories(
    queryEmbedding: FloatArray,
    memories: List<MemoryEntity>,
    topK: Int = 5,
    threshold: Float = 0.65f
): List<ScoredMemory> {
    return memories
        .filter { it.embedding != null }
        .map { memory ->
            ScoredMemory(
                memory = memory,
                score = cosineSimilarity(queryEmbedding, memory.embedding!!.toFloatArray())
            )
        }
        .filter { it.score >= threshold }
        .sortedByDescending { it.score }
        .take(topK)
}

fun searchSimilarChunks(
    queryEmbedding: FloatArray,
    chunks: List<ConversationChunkEntity>,
    topK: Int = 3,
    threshold: Float = 0.65f,
    messageEndIdxMax: Int? = null
): List<ScoredChunk> {
    return chunks
        .filter { it.embedding != null }
        .filter { messageEndIdxMax == null || it.messageEndIdx < messageEndIdxMax }
        .map { chunk ->
            ScoredChunk(
                chunk = chunk,
                score = cosineSimilarity(queryEmbedding, chunk.embedding!!.toFloatArray())
            )
        }
        .filter { it.score >= threshold }
        .sortedByDescending { it.score }
        .take(topK)
}
