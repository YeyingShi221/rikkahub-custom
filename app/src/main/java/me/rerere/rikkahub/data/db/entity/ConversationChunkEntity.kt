package me.rerere.rikkahub.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversation_chunk")
data class ConversationChunkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("assistant_id") val assistantId: String,
    @ColumnInfo("content") val content: String,
    @ColumnInfo("content_hash", defaultValue = "0") val contentHash: Int = 0,
    @ColumnInfo("embedding") val embedding: ByteArray? = null,
    @ColumnInfo("chunk_index") val chunkIndex: Int = 0,
    @ColumnInfo("created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "message_end_idx", defaultValue = "0") val messageEndIdx: Int = 0,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConversationChunkEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id
}
