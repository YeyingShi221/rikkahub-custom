package me.rerere.rikkahub.ui.pages.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Calendar01
import me.rerere.hugeicons.stroke.Cancel01
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import me.rerere.rikkahub.data.model.Conversation
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    visible: Boolean,
    conversations: List<Conversation>,
    onDismiss: () -> Unit,
    onExport: (startDate: LocalDate?, endDate: LocalDate?, conversationIds: Set<Uuid>?) -> Unit
) {
    if (!visible) return

    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    var selectSpecific by remember { mutableStateOf(false) }
    val selectedConversations = remember { mutableStateListOf<Uuid>() }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val formatter = remember {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        startDate = instant.toLocalDateTime(TimeZone.UTC).date
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        endDate = instant.toLocalDateTime(TimeZone.UTC).date
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val exportEnabled = !selectSpecific || selectedConversations.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Export Chat History") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Date Range
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Date Range",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startDate?.toString() ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Start Date") },
                            placeholder = { Text("All time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                                LaunchedEffect(interactionSource) {
                                    interactionSource.interactions.collect {
                                        if (it is PressInteraction.Release) {
                                            showStartDatePicker = true
                                        }
                                    }
                                }
                            },
                            trailingIcon = {
                                if (startDate != null) {
                                    IconButton(onClick = { startDate = null }) {
                                        Icon(HugeIcons.Cancel01, contentDescription = "Clear")
                                    }
                                } else {
                                    IconButton(onClick = { showStartDatePicker = true }) {
                                        Icon(HugeIcons.Calendar01, contentDescription = "Select Start Date")
                                    }
                                }
                            }
                        )

                        OutlinedTextField(
                            value = endDate?.toString() ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("End Date") },
                            placeholder = { Text("All time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                                LaunchedEffect(interactionSource) {
                                    interactionSource.interactions.collect {
                                        if (it is PressInteraction.Release) {
                                            showEndDatePicker = true
                                        }
                                    }
                                }
                            },
                            trailingIcon = {
                                if (endDate != null) {
                                    IconButton(onClick = { endDate = null }) {
                                        Icon(HugeIcons.Cancel01, contentDescription = "Clear")
                                    }
                                } else {
                                    IconButton(onClick = { showEndDatePicker = true }) {
                                        Icon(HugeIcons.Calendar01, contentDescription = "Select End Date")
                                    }
                                }
                            }
                        )
                    }
                }

                HorizontalDivider()

                // Section 2: Conversations
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Conversations",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.RadioButton) { selectSpecific = false }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = !selectSpecific,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "All conversations in date range", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.RadioButton) { selectSpecific = true }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectSpecific,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Select specific", style = MaterialTheme.typography.bodyMedium)
                    }

                    if (selectSpecific) {
                        val filteredConversations = remember(conversations, startDate, endDate) {
                            conversations.filter { conv ->
                                val convDate = Instant.fromEpochMilliseconds(conv.updateAt.toEpochMilli())
                                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                                val afterStart = startDate?.let { convDate >= it } ?: true
                                val beforeEnd = endDate?.let { convDate <= it } ?: true
                                afterStart && beforeEnd
                            }
                        }

                        if (filteredConversations.isEmpty()) {
                            Text(
                                text = "No conversations found in selected date range.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 32.dp, top = 8.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                            ) {
                                items(filteredConversations, key = { it.id.toString() }) { conversation ->
                                    val isSelected = selectedConversations.contains(conversation.id)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .toggleable(
                                                value = isSelected,
                                                onValueChange = {
                                                    if (it) {
                                                        selectedConversations.add(conversation.id)
                                                    } else {
                                                        selectedConversations.remove(conversation.id)
                                                    }
                                                },
                                                role = Role.Checkbox
                                            )
                                            .padding(start = 32.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = conversation.title.ifEmpty { "New Conversation" },
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = formatter.format(conversation.updateAt),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val ids = if (selectSpecific) selectedConversations.toSet() else null
                    onExport(startDate, endDate, ids)
                    onDismiss()
                },
                enabled = exportEnabled
            ) {
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
