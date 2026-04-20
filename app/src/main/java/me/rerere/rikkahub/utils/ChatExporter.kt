package me.rerere.rikkahub.utils

import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.model.Conversation

object ChatExporter {

    /**
     * Export a single conversation to formatted text with real names.
     * Optionally filter by date range.
     */
    fun exportToText(
        conversation: Conversation,
        userName: String,
        assistantName: String,
        startDate: kotlinx.datetime.LocalDate? = null,
        endDate: kotlinx.datetime.LocalDate? = null,
    ): String {
        return exportConversations(
            conversations = listOf(conversation),
            userName = userName,
            assistantName = assistantName,
            startDate = startDate,
            endDate = endDate,
        )
    }

    /**
     * Export multiple conversations (per-assistant) to formatted text.
     * Messages are merged chronologically with date headers.
     * Optionally filter by date range.
     */
    fun exportConversations(
        conversations: List<Conversation>,
        userName: String,
        assistantName: String,
        startDate: kotlinx.datetime.LocalDate? = null,
        endDate: kotlinx.datetime.LocalDate? = null,
    ): String {
        data class TimedMessage(
            val message: UIMessage,
            val conversationTitle: String,
        )

        // Collect all messages across conversations, filter by role and date
        val allMessages = conversations.flatMap { conv ->
            conv.currentMessages
                .filter { msg ->
                    msg.role == MessageRole.USER || msg.role == MessageRole.ASSISTANT
                }
                .filter { msg ->
                    val msgDate = msg.createdAt.date
                    (startDate == null || msgDate >= startDate) &&
                        (endDate == null || msgDate <= endDate)
                }
                .map { TimedMessage(it, conv.title) }
        }.sortedBy { it.message.createdAt }

        if (allMessages.isEmpty()) return ""

        val sb = StringBuilder()
        sb.appendLine("【Beginning of Logs】")
        sb.appendLine()

        var currentDateStr: String? = null
        var currentConvTitle: String? = null

        for (timed in allMessages) {
            val message = timed.message
            val dt = message.createdAt
            val dateStr = "%04d/%02d/%02d".format(dt.year, dt.monthNumber, dt.dayOfMonth)

            if (dateStr != currentDateStr) {
                if (currentDateStr != null) {
                    sb.appendLine()
                }
                sb.appendLine("--- $dateStr ---")
                sb.appendLine()
                currentDateStr = dateStr
            }

            // Show conversation title header when it changes (for multi-conversation export)
            if (conversations.size > 1 && timed.conversationTitle != currentConvTitle) {
                sb.appendLine("  [${timed.conversationTitle}]")
                sb.appendLine()
                currentConvTitle = timed.conversationTitle
            }

            val speakerName = when (message.role) {
                MessageRole.USER -> userName
                MessageRole.ASSISTANT -> assistantName
                else -> continue
            }

            val textContent = message.parts
                .filterIsInstance<UIMessagePart.Text>()
                .joinToString("") { it.text }
                .trim()

            if (textContent.isNotEmpty()) {
                sb.appendLine("▶ $speakerName:")
                sb.appendLine(textContent)
                sb.appendLine()
            }
        }

        sb.appendLine("【End of Logs】")
        return sb.toString().trimEnd()
    }

    fun generateFilename(
        conversationTitle: String,
        assistantName: String,
        startDate: kotlinx.datetime.LocalDate? = null,
        endDate: kotlinx.datetime.LocalDate? = null,
    ): String {
        val sanitized = conversationTitle
            .replace(Regex("[^a-zA-Z0-9\\u4e00-\\u9fff\\s-]"), "")
            .trim()
            .take(30)
        val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
        val dateStr = if (startDate != null && endDate != null) {
            "${startDate.toString().replace("-", "")}_${endDate.toString().replace("-", "")}"
        } else {
            sdf.format(java.util.Date())
        }
        return "chat_${assistantName}_${sanitized}_$dateStr.txt"
    }
}
