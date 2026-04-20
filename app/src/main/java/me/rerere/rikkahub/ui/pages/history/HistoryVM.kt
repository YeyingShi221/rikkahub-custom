package me.rerere.rikkahub.ui.pages.history

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.data.datastore.getCurrentAssistant
import me.rerere.rikkahub.data.model.Conversation
import me.rerere.rikkahub.data.repository.ConversationRepository
import me.rerere.rikkahub.utils.ChatExporter
import kotlin.uuid.Uuid

private const val TAG = "HistoryVM"

class HistoryVM(
    private val conversationRepo: ConversationRepository,
    private val settingsStore: SettingsStore,
    private val application: Application,  // NEW: for contentResolver
) : ViewModel() {
    val assistant = settingsStore.settingsFlow
        .map { it.getCurrentAssistant() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val conversations = assistant.flatMapLatest { assistant ->
        conversationRepo.getConversationsOfAssistant(assistant?.id ?: Uuid.random())
    }.catch {
        Log.e(TAG, "Error: ${it.message}")
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun deleteConversation(conversation: Conversation) {
        viewModelScope.launch {
            conversationRepo.deleteConversation(conversation)
        }
    }

    fun deleteAllConversations() {
        val assistant = assistant.value ?: return
        viewModelScope.launch {
            conversationRepo.deleteConversationOfAssistant(assistant.id)
        }
    }

    fun togglePinStatus(conversationId: Uuid) {
        viewModelScope.launch {
            conversationRepo.togglePinStatus(conversationId)
        }
    }

    fun getPinnedConversations(): Flow<List<Conversation>> =
        conversationRepo.getPinnedConversations()

    fun restoreConversation(conversation: Conversation) {
        viewModelScope.launch {
            conversationRepo.insertConversation(conversation)
        }
    }

    suspend fun getFullConversation(conversationId: Uuid): Conversation? {
        return conversationRepo.getConversationById(conversationId)
    }

    // NEW: Export logic
    suspend fun exportConversations(
        startDate: LocalDate?,
        endDate: LocalDate?,
        conversationIds: Set<Uuid>?,
    ): Result<String> = runCatching {
        val settings = settingsStore.settingsFlow.first()
        val currentAssistant = assistant.value
            ?: error("No current assistant selected")

        val userName = settings.displaySetting.userNickname.ifBlank { "User" }
        val assistantName = currentAssistant.name.ifBlank { "Assistant" }

        // Resolve which conversations to export
        val idsToFetch = conversationIds
            ?: conversations.value.map { it.id }.toSet()

        if (idsToFetch.isEmpty()) error("No conversations to export")

        // Fetch full conversations (with messages) since list view may be lightweight
        val fullConversations = idsToFetch.mapNotNull { id ->
            conversationRepo.getConversationById(id)
        }

        if (fullConversations.isEmpty()) error("Failed to load conversations")

        val content = ChatExporter.exportConversations(
            conversations = fullConversations,
            userName = userName,
            assistantName = assistantName,
            startDate = startDate,
            endDate = endDate,
        )

        if (content.isBlank()) error("No messages in selected date range")

        // Filename: single conv uses its title, multi uses "All"
        val titleForFilename = if (fullConversations.size == 1) {
            fullConversations[0].title.ifBlank { "conversation" }
        } else {
            "All"
        }

        val filename = ChatExporter.generateFilename(
            conversationTitle = titleForFilename,
            assistantName = assistantName,
            startDate = startDate,
            endDate = endDate,
        )

        writeToDownloads(filename, content)
        filename
    }.onFailure {
        Log.e(TAG, "Export failed: ${it.message}", it)
    }

    private fun writeToDownloads(filename: String, content: String) {
        val resolver = application.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val uri = resolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            values
        ) ?: error("Failed to create file in Downloads")

        resolver.openOutputStream(uri)?.use { stream ->
            stream.write(content.toByteArray(Charsets.UTF_8))
        } ?: error("Failed to open output stream")
    }
}