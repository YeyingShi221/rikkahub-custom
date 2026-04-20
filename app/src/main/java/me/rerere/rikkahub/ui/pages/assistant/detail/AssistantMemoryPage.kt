package me.rerere.rikkahub.ui.pages.assistant.detail

import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.PencilEdit01
import me.rerere.hugeicons.stroke.Add01
import me.rerere.hugeicons.stroke.Delete01
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.rikkahub.R
import me.rerere.rikkahub.data.model.Assistant
import me.rerere.rikkahub.data.model.AssistantMemory
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.components.ui.RikkaConfirmDialog
import me.rerere.rikkahub.ui.hooks.EditStateContent
import me.rerere.rikkahub.ui.hooks.useEditState
import me.rerere.rikkahub.ui.theme.CustomColors
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

import me.rerere.hugeicons.stroke.Tick01
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.platform.LocalContext

@Composable
fun AssistantMemoryPage(id: String) {
    val vm: AssistantDetailVM = koinViewModel(
        parameters = {
            parametersOf(id)
        }
    )
    val assistant by vm.assistant.collectAsStateWithLifecycle()
    val memories by vm.memories.collectAsStateWithLifecycle()
    val totalChunks by vm.totalChunks.collectAsStateWithLifecycle()
    val embeddedChunks by vm.embeddedChunks.collectAsStateWithLifecycle()
    val chunkSize by vm.chunkSize.collectAsStateWithLifecycle()
    val overlapPercent by vm.overlapPercent.collectAsStateWithLifecycle()
    val isVectorizing by vm.isVectorizing.collectAsStateWithLifecycle()
    val vectorizationProgress by vm.vectorizationProgress.collectAsStateWithLifecycle()
    val debugStatus by vm.debugStatus.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(stringResource(R.string.assistant_page_tab_memory))
                },
                navigationIcon = {
                    BackButton()
                },
                scrollBehavior = scrollBehavior,
                colors = CustomColors.topBarColors,
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = CustomColors.topBarColors.containerColor,
    ) { innerPadding ->
        AssistantMemoryContent(
            modifier = Modifier.padding(innerPadding),
            assistant = assistant,
            memories = memories,
            isVectorizing = isVectorizing,
            vectorizationProgress = vectorizationProgress,
            totalChunks = totalChunks,
            embeddedChunks = embeddedChunks,
            chunkSize = chunkSize,
            overlapPercent = overlapPercent,
            onUpdateAssistant = { vm.update(it) },
            onDeleteMemory = { vm.deleteMemory(it) },
            onAddMemory = { vm.addMemory(it) },
            onUpdateMemory = { vm.updateMemory(it) },
            onVectorizeChat = {
                vm.vectorizeChat { count ->
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        Toast.makeText(context, "Chunks: $count | ${vm.debugStatus.value}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onClearChatVectors = { vm.clearChatVectors() },
            onSetChunkSize = { vm.setChunkSize(it) },
            onSetOverlapPercent = { vm.setOverlapPercent(it) },
        )
    }
}

@Composable
private fun AssistantMemoryContent(
    modifier: Modifier = Modifier,
    assistant: Assistant,
    memories: List<AssistantMemory>,
    isVectorizing: Boolean,
    vectorizationProgress: Float,
    totalChunks: Int,
    embeddedChunks: Int,
    chunkSize: Int,
    overlapPercent: Int,
    onUpdateAssistant: (Assistant) -> Unit,
    onAddMemory: (AssistantMemory) -> Unit,
    onUpdateMemory: (AssistantMemory) -> Unit,
    onDeleteMemory: (AssistantMemory) -> Unit,
    onVectorizeChat: () -> Unit,
    onClearChatVectors: () -> Unit,
    onSetChunkSize: (Int) -> Unit,
    onSetOverlapPercent: (Int) -> Unit,
) {
    val memoryDialogState = useEditState<AssistantMemory> {
        if (it.id == 0) {
            onAddMemory(it)
        } else {
            onUpdateMemory(it)
        }
    }
    var pendingDeleteMemory by remember { mutableStateOf<AssistantMemory?>(null) }

    // 记忆对话框
    memoryDialogState.EditStateContent { memory, update ->
        AlertDialog(
            onDismissRequest = {
                memoryDialogState.dismiss()
            },
            title = {
                Text(stringResource(R.string.assistant_page_manage_memory_title))
            },
            text = {
                TextField(
                    value = memory.content,
                    onValueChange = {
                        update(memory.copy(content = it))
                    },
                    label = {
                        Text(stringResource(R.string.assistant_page_manage_memory_title))
                    },
                    minLines = 2,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        memoryDialogState.confirm()
                    }
                ) {
                    Text(stringResource(R.string.assistant_page_save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        memoryDialogState.dismiss()
                    }
                ) {
                    Text(stringResource(R.string.assistant_page_cancel))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Chat Vectorization Section
        Text(
            text = "Chat Vectorization",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Display
                val chatPercent = if (totalChunks > 0) (embeddedChunks * 100 / totalChunks) else 0
                Column {
                    Text(
                        text = "Chunks: $totalChunks | Embedded: $embeddedChunks/$totalChunks ($chatPercent%)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Unique Hashes: $totalChunks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Configurable Sliders
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Chunk Size (chars)", style = MaterialTheme.typography.bodySmall)
                        Text("$chunkSize", style = MaterialTheme.typography.bodySmall)
                    }
                    androidx.compose.material3.Slider(
                        value = chunkSize.toFloat(),
                        onValueChange = { onSetChunkSize(it.toInt()) },
                        valueRange = 1000f..8000f,
                        steps = ((8000 - 1000) / 500) - 1
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Overlap (%)", style = MaterialTheme.typography.bodySmall)
                        Text("$overlapPercent%", style = MaterialTheme.typography.bodySmall)
                    }
                    androidx.compose.material3.Slider(
                        value = overlapPercent.toFloat(),
                        onValueChange = { onSetOverlapPercent(it.toInt()) },
                        valueRange = 0f..30f,
                        steps = (30 / 5) - 1
                    )
                }

                // Action Buttons
                var showClearDialog by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showClearDialog = true },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Clear Vectors")
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    androidx.compose.material3.Button(
                        onClick = onVectorizeChat,
                        enabled = !isVectorizing
                    ) {
                        Text("Vectorize Chat")
                    }
                }
                
                if (showClearDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearDialog = false },
                        title = { Text("Clear Chat Vectors?") },
                        text = { Text("This will delete all chunks and embeddings for the current assistant.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    onClearChatVectors()
                                    showClearDialog = false
                                },
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Clear")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (isVectorizing) {
                    LinearProgressIndicator(
                        progress = { vectorizationProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // RAG Injection Settings
        Text(
            text = "RAG Injection",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Enable toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable RAG Injection")
                    Switch(
                        checked = assistant.ragEnabled,
                        onCheckedChange = { onUpdateAssistant(assistant.copy(ragEnabled = it)) }
                    )
                }

                if (assistant.ragEnabled) {
                    // Position dropdown
                    Text("Injection Position", style = MaterialTheme.typography.bodySmall)
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        me.rerere.rikkahub.data.model.InjectionPosition.entries.forEach { pos ->
                            androidx.compose.material3.FilterChip(
                                selected = assistant.ragPosition == pos,
                                onClick = { onUpdateAssistant(assistant.copy(ragPosition = pos)) },
                                label = { Text(pos.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    // Depth (only if AT_DEPTH)
                    if (assistant.ragPosition == me.rerere.rikkahub.data.model.InjectionPosition.AT_DEPTH) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Depth: ${assistant.ragDepth}")
                            androidx.compose.material3.Slider(
                                value = assistant.ragDepth.toFloat(),
                                onValueChange = { onUpdateAssistant(assistant.copy(ragDepth = it.toInt())) },
                                valueRange = 1f..20f,
                                steps = 18,
                                modifier = Modifier.weight(1f).padding(start = 16.dp)
                            )
                        }
                    }

                    // Role selector
                    Text("Inject As", style = MaterialTheme.typography.bodySmall)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.FilterChip(
                            selected = assistant.ragRole == me.rerere.ai.core.MessageRole.USER,
                            onClick = { onUpdateAssistant(assistant.copy(ragRole = me.rerere.ai.core.MessageRole.USER)) },
                            label = { Text("User") }
                        )
                        androidx.compose.material3.FilterChip(
                            selected = assistant.ragRole == me.rerere.ai.core.MessageRole.ASSISTANT,
                            onClick = { onUpdateAssistant(assistant.copy(ragRole = me.rerere.ai.core.MessageRole.ASSISTANT)) },
                            label = { Text("Assistant") }
                        )
                    }

                    // Top K
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Top K: ${assistant.ragTopK}")
                        androidx.compose.material3.Slider(
                            value = assistant.ragTopK.toFloat(),
                            onValueChange = { onUpdateAssistant(assistant.copy(ragTopK = it.toInt())) },
                            valueRange = 1f..10f,
                            steps = 8,
                            modifier = Modifier.weight(1f).padding(start = 16.dp)
                        )
                    }

                    // Threshold
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Threshold: ${"%.2f".format(assistant.ragThreshold)}")
                        androidx.compose.material3.Slider(
                            value = assistant.ragThreshold,
                            onValueChange = { onUpdateAssistant(assistant.copy(ragThreshold = "%.2f".format(it).toFloat())) },
                            valueRange = 0.3f..0.9f,
                            modifier = Modifier.weight(1f).padding(start = 16.dp)
                        )
                    }

                    // Retain gate: skip N most recent messages (already in LLM context)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Skip Recent: ${assistant.ragSkipRecentMessages}")
                        androidx.compose.material3.Slider(
                            value = assistant.ragSkipRecentMessages.toFloat(),
                            onValueChange = { onUpdateAssistant(assistant.copy(ragSkipRecentMessages = it.toInt())) },
                            valueRange = 0f..30f,
                            steps = 29,
                            modifier = Modifier.weight(1f).padding(start = 16.dp)
                        )
                    }
                    Text(
                        "Threshold: lower = more matches, higher = stricter. Skip Recent: avoid duplicating chunks already in context — tune up for long convos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        CardGroup {
            item(
                headlineContent = { Text(stringResource(R.string.assistant_page_memory)) },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.assistant_page_memory_desc),
                    )
                },
                trailingContent = {
                    Switch(
                        checked = assistant.enableMemory,
                        onCheckedChange = {
                            onUpdateAssistant(
                                assistant.copy(
                                    enableMemory = it
                                )
                            )
                        }
                    )
                }
            )
            item(
                headlineContent = { Text(stringResource(R.string.assistant_page_global_memory)) },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.assistant_page_global_memory_desc),
                    )
                },
                trailingContent = {
                    Switch(
                        checked = assistant.useGlobalMemory,
                        onCheckedChange = {
                            onUpdateAssistant(
                                assistant.copy(
                                    useGlobalMemory = it
                                )
                            )
                        },
                        enabled = assistant.enableMemory
                    )
                }
            )
            item(
                headlineContent = { Text(stringResource(R.string.assistant_page_recent_chats)) },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.assistant_page_recent_chats_desc),
                    )
                },
                trailingContent = {
                    Switch(
                        checked = assistant.enableRecentChatsReference,
                        onCheckedChange = {
                            onUpdateAssistant(
                                assistant.copy(
                                    enableRecentChatsReference = it
                                )
                            )
                        }
                    )
                }
            )
            item(
                headlineContent = { Text(stringResource(R.string.assistant_page_time_reminder)) },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.assistant_page_time_reminder_desc),
                    )
                },
                trailingContent = {
                    Switch(
                        checked = assistant.enableTimeReminder,
                        onCheckedChange = {
                            onUpdateAssistant(
                                assistant.copy(
                                    enableTimeReminder = it
                                )
                            )
                        }
                    )
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.assistant_page_manage_memory_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .align(Alignment.CenterStart)
            )

            IconButton(
                onClick = {
                    memoryDialogState.open(AssistantMemory(0, ""))
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = HugeIcons.Add01,
                    contentDescription = null
                )
            }
        }

        memories.fastForEach { memory ->
            key(memory.id) {
                MemoryItem(
                    memory = memory,
                    onEditMemory = {
                        memoryDialogState.open(it)
                    },
                    onDeleteMemory = {
                        pendingDeleteMemory = it
                    }
                )
            }
        }
        
        Spacer(Modifier.height(32.dp))
    }

    RikkaConfirmDialog(
        show = pendingDeleteMemory != null,
        title = stringResource(R.string.confirm_delete),
        confirmText = stringResource(R.string.confirm),
        dismissText = stringResource(R.string.cancel),
        onConfirm = {
            pendingDeleteMemory?.let(onDeleteMemory)
            pendingDeleteMemory = null
        },
        onDismiss = { pendingDeleteMemory = null },
        text = {
            Text(
                text = pendingDeleteMemory?.content.orEmpty(),
                maxLines = 8,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
private fun MemoryItem(
    memory: AssistantMemory,
    onEditMemory: (AssistantMemory) -> Unit,
    onDeleteMemory: (AssistantMemory) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CustomColors.cardColorsOnSurfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "#${memory.id}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    
                }
                Text(
                    text = memory.content,

                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(
                onClick = { onEditMemory(memory) }
            ) {
                Icon(HugeIcons.PencilEdit01, null)
            }
            IconButton(
                onClick = { onDeleteMemory(memory) }
            ) {
                Icon(
                    HugeIcons.Delete01,
                    stringResource(R.string.assistant_page_delete)
                )
            }
        }
    }
}
