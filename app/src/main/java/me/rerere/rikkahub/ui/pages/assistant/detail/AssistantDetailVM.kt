package me.rerere.rikkahub.ui.pages.assistant.detail

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.rerere.rikkahub.data.datastore.Settings
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.data.files.FilesManager
import me.rerere.rikkahub.data.files.SkillManager
import me.rerere.rikkahub.data.files.SkillMetadata
import me.rerere.rikkahub.data.model.Assistant
import me.rerere.rikkahub.data.model.AssistantMemory
import me.rerere.rikkahub.data.model.Avatar
import me.rerere.rikkahub.data.model.Tag
import me.rerere.rikkahub.data.ai.EmbeddingService
import me.rerere.rikkahub.data.db.entity.toByteArray
import me.rerere.rikkahub.data.repository.MemoryRepository
import kotlin.uuid.Uuid

import kotlinx.coroutines.flow.first
import me.rerere.rikkahub.data.repository.ConversationChunkRepository
import me.rerere.rikkahub.data.repository.ConversationRepository

private const val TAG = "AssistantDetailVM"

class AssistantDetailVM(
    private val id: String,
    private val settingsStore: SettingsStore,
    private val memoryRepository: MemoryRepository,
    private val filesManager: FilesManager,
    private val skillManager: SkillManager,
    private val embeddingService: EmbeddingService,
    private val conversationChunkRepository: ConversationChunkRepository,
    private val conversationRepo: ConversationRepository,
) : ViewModel() {
    private val assistantId = Uuid.parse(id)

    private val _totalChunks = MutableStateFlow(0)
    val totalChunks = _totalChunks.asStateFlow()

    private val _embeddedChunks = MutableStateFlow(0)
    val embeddedChunks = _embeddedChunks.asStateFlow()

    private val _chunkSize = MutableStateFlow(4000)
    val chunkSize = _chunkSize.asStateFlow()

    private val _overlapPercent = MutableStateFlow(15)
    val overlapPercent = _overlapPercent.asStateFlow()

    private val _isVectorizing = MutableStateFlow(false)
    val isVectorizing = _isVectorizing.asStateFlow()

    private val _vectorizationProgress = MutableStateFlow(0f)
    val vectorizationProgress = _vectorizationProgress.asStateFlow()

    private val _debugStatus = MutableStateFlow("")
    val debugStatus = _debugStatus.asStateFlow()

    private val _skills = MutableStateFlow<List<SkillMetadata>>(emptyList())
    val skills = _skills.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _skills.value = skillManager.listSkills()
            updateChunkCounts()
        }
    }

    private suspend fun updateChunkCounts() {
        val count = conversationChunkRepository.getChunkCountOfAssistant(assistantId.toString())
        val embeddedCount = conversationChunkRepository.getEmbeddedChunkCountOfAssistant(assistantId.toString())
        _totalChunks.value = count
        _embeddedChunks.value = embeddedCount
    }

    fun setChunkSize(size: Int) {
        _chunkSize.value = size
        update(assistant.value.copy(chunkSize = size))
    }

    fun setOverlapPercent(percent: Int) {
        _overlapPercent.value = percent
        update(assistant.value.copy(overlapPercent = percent))
    }

    fun clearChatVectors() {
        viewModelScope.launch(Dispatchers.IO) {
            conversationChunkRepository.clearAllChunks(assistantId.toString())
            updateChunkCounts()
        }
    }

    fun vectorizeChat(onComplete: (Int) -> Unit) {
        _debugStatus.value = "Starting... configured=${embeddingService.isConfigured}, assistantId=$assistantId"
        if (!embeddingService.isConfigured) {
            _debugStatus.value = "ERROR: No API key configured!"
            onComplete(0)
            return
        }
        if (_isVectorizing.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isVectorizing.value = true
            _vectorizationProgress.value = 0f

            try {
                val summaries = conversationRepo.getConversationsOfAssistant(assistantId).first()
                _debugStatus.value = "Found ${summaries.size} conversations for $assistantId"
                Log.d(TAG, "vectorizeChat: found ${summaries.size} conversations for assistant $assistantId")
                val allMessages = mutableListOf<me.rerere.ai.ui.UIMessage>()
                
                summaries.forEachIndexed { index, summary ->
                    val conv = conversationRepo.getConversationById(summary.id)
                    if (conv != null) {
                        allMessages.addAll(conv.messageNodes.flatMap { it.messages })
                    }
                    // Updating progress partially for fetching, but mostly it's fast
                    _vectorizationProgress.value = (index + 1) * 0.1f / summaries.size
                }

                // The chunkAndStoreConversation handles the actual chunking, embedding, storing
                // We'll let it block here, ideally we could pass a progress callback but for now it's fine.
                _debugStatus.value = "Collected ${allMessages.size} msgs from ${summaries.size} convs. Chunking..."
                Log.d(TAG, "vectorizeChat: collected ${allMessages.size} messages total")
                _vectorizationProgress.value = 0.5f

                conversationChunkRepository.chunkAndStoreConversation(
                    assistantId = assistantId.toString(),
                    messages = allMessages,
                    chunkSize = _chunkSize.value,
                    overlapPercent = _overlapPercent.value,
                    embeddingService = embeddingService,
                    onProgress = { current, total ->
                        _vectorizationProgress.value = 0.5f + 0.5f * (current.toFloat() / total)
                        _debugStatus.value = "Embedding: $current / $total chunks"
                    }
                )

                updateChunkCounts()
                val total = _totalChunks.value
                _isVectorizing.value = false
                onComplete(total)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to vectorize chat", e)
                _isVectorizing.value = false
                onComplete(0)
            }
        }
    }

    val settings: StateFlow<Settings> =
        settingsStore.settingsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, Settings.dummy())

    val mcpServerConfigs = settingsStore
        .settingsFlow.map { settings ->
            settings.mcpServers
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = emptyList()
        )

    val assistant: StateFlow<Assistant> = settingsStore
        .settingsFlow
        .map { settings ->
            settings.assistants.find { it.id == assistantId } ?: Assistant()
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = Assistant()
        )

    init {
        // Load persisted chunk settings once assistant is available
        viewModelScope.launch {
            val a = assistant.first { it.id != kotlin.uuid.Uuid.NIL }
            _chunkSize.value = a.chunkSize
            _overlapPercent.value = a.overlapPercent
        }
    }

    val memories = assistant
        .flatMapLatest { currentAssistant ->
            if (currentAssistant.useGlobalMemory) {
                memoryRepository.getGlobalMemoriesFlow()
            } else {
                memoryRepository.getMemoriesOfAssistantFlow(assistantId.toString())
            }
        }
        .stateIn(
            scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = emptyList()
        )

    val providers = settingsStore
        .settingsFlow
        .map { settings ->
            settings.providers
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = emptyList()
        )

    val tags = settingsStore
        .settingsFlow
        .map { settings ->
            settings.assistantTags
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = emptyList()
        )

    fun updateTags(tagIds: List<Uuid>, tags: List<Tag>) {
        viewModelScope.launch {
            val settings = settings.value
            settingsStore.update(
                settings = settings.copy(
                    assistantTags = tags
                )
            )
            update(
                assistant.value.copy(
                    tags = tagIds.toList()
                )
            )
            Log.d(TAG, "updateTags: ${tagIds.joinToString(",")}")
            cleanupUnusedTags()
        }
    }

    fun cleanupUnusedTags() {
        viewModelScope.launch {
            val settings = settings.value
            val validTagIds = settings.assistantTags.map { it.id }.toSet()

            // 清理 assistant 中的无效 tag id
            val cleanedAssistants = settings.assistants.map { assistant ->
                val validTags = assistant.tags.filter { tagId ->
                    validTagIds.contains(tagId)
                }
                if (validTags.size != assistant.tags.size) {
                    assistant.copy(tags = validTags)
                } else {
                    assistant
                }
            }

            // 获取清理后的 assistant 中使用的 tag id
            val usedTagIds = cleanedAssistants.flatMap { it.tags }.toSet()

            // 清理未使用的 tags
            val cleanedTags = settings.assistantTags.filter { tag ->
                usedTagIds.contains(tag.id)
            }

            // 检查是否需要更新
            val needUpdateAssistants = cleanedAssistants != settings.assistants
            val needUpdateTags = cleanedTags.size != settings.assistantTags.size

            if (needUpdateAssistants || needUpdateTags) {
                settingsStore.update(
                    settings = settings.copy(
                        assistants = cleanedAssistants,
                        assistantTags = cleanedTags
                    )
                )
            }
        }
    }

    fun update(assistant: Assistant) {
        viewModelScope.launch {
            val settings = settings.value
            settingsStore.update(
                settings = settings.copy(
                    assistants = settings.assistants.map {
                        if (it.id == assistant.id) {
                            checkAvatarDelete(old = it, new = assistant) // 删除旧头像
                            checkBackgroundDelete(old = it, new = assistant) // 删除旧背景
                            assistant
                        } else {
                            it
                        }
                    })
            )
        }
    }

    fun addMemory(memory: AssistantMemory) {
        viewModelScope.launch {
            val memoryAssistantId = if (assistant.value.useGlobalMemory) {
                MemoryRepository.GLOBAL_MEMORY_ID
            } else {
                assistantId.toString()
            }
            memoryRepository.addMemory(
                assistantId = memoryAssistantId,
                content = memory.content
            )
        }
    }

    fun updateMemory(memory: AssistantMemory) {
        viewModelScope.launch {
            memoryRepository.updateContent(id = memory.id, content = memory.content)
        }
    }

    fun deleteMemory(memory: AssistantMemory) {
        viewModelScope.launch {
            memoryRepository.deleteMemory(id = memory.id)
        }
    }

    fun vectorizeAllMemories(onComplete: (Int) -> Unit) {
        Log.d(TAG, "vectorizeAllMemories called, embeddingService configured: ${embeddingService.isConfigured}")
        if (!embeddingService.isConfigured) {
            Log.w(TAG, "EmbeddingService has no API key, skipping vectorization")
            onComplete(0)
            return
        }
        if (_isVectorizing.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isVectorizing.value = true
            _vectorizationProgress.value = 0f

            val assistantIdStr = if (assistant.value.useGlobalMemory) {
                MemoryRepository.GLOBAL_MEMORY_ID
            } else {
                assistantId.toString()
            }
            Log.d(TAG, "vectorize: assistantId=$assistantIdStr")

            val allMemories = memoryRepository.getMemoryEntitiesOfAssistant(assistantIdStr)
            val toProcess = allMemories.filter { it.embedding == null }
            Log.d(TAG, "vectorize: total=${allMemories.size}, toProcess=${toProcess.size}")

            if (toProcess.isEmpty()) {
                _isVectorizing.value = false
                onComplete(0)
                return@launch
            }

            var count = 0
            toProcess.forEachIndexed { index, memory ->
                try {
                    val embedding = embeddingService.embed(memory.content)
                    if (embedding != null) {
                        memoryRepository.updateEmbedding(memory.id, embedding.toByteArray())
                        count++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to vectorize memory ${memory.id}", e)
                }
                _vectorizationProgress.value = (index + 1).toFloat() / toProcess.size
            }

            _isVectorizing.value = false
            onComplete(count)
        }
    }

    fun checkAvatarDelete(old: Assistant, new: Assistant) {
        if (old.avatar is Avatar.Image && old.avatar != new.avatar) {
            filesManager.deleteChatFiles(listOf(old.avatar.url.toUri()))
        }
    }

    fun checkBackgroundDelete(old: Assistant, new: Assistant) {
        val oldBackground = old.background
        val newBackground = new.background

        if (oldBackground != null && oldBackground != newBackground) {
            try {
                val oldUri = oldBackground.toUri()
                if (oldUri.scheme == "content" || oldUri.scheme == "file") {
                    filesManager.deleteChatFiles(listOf(oldUri))
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete background file: $oldBackground", e)
            }
        }
    }
}
