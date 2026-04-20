package me.rerere.rikkahub.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import me.rerere.rikkahub.data.db.entity.ConversationChunkEntity

@Dao
interface ConversationChunkDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunk(chunk: ConversationChunkEntity): Long

    @Query("UPDATE conversation_chunk SET embedding = :embedding WHERE id = :id")
    suspend fun updateEmbedding(id: Int, embedding: ByteArray)

    @Query("SELECT * FROM conversation_chunk WHERE assistant_id = :assistantId ORDER BY chunk_index ASC")
    suspend fun getChunksOfAssistant(assistantId: String): List<ConversationChunkEntity>

    @Query("SELECT COUNT(*) FROM conversation_chunk WHERE assistant_id = :assistantId")
    suspend fun getChunkCountOfAssistant(assistantId: String): Int

    @Query("SELECT COUNT(*) FROM conversation_chunk WHERE assistant_id = :assistantId AND embedding IS NOT NULL")
    suspend fun getEmbeddedChunkCountOfAssistant(assistantId: String): Int

    @Query("UPDATE conversation_chunk SET embedding = NULL WHERE assistant_id = :assistantId")
    suspend fun clearAllEmbeddings(assistantId: String)

    @Query("DELETE FROM conversation_chunk WHERE assistant_id = :assistantId")
    suspend fun clearAllChunks(assistantId: String)

    @Query("DELETE FROM conversation_chunk WHERE assistant_id = :assistantId")
    suspend fun deleteAllChunks(assistantId: String)
}
