package com.app.fintrack.presentation.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.fintrack.domain.model.AiMessage
import com.app.fintrack.presentation.theme.FintrackSpacingTokens
import kotlinx.datetime.LocalDateTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun AiAssistantRoute(
    viewModel: AiAssistantViewModel = koinViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    AiAssistantScreen(
        state = state,
        onSend = viewModel::send,
        onRetry = viewModel::retryLast,
        onDismissError = viewModel::dismissError,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AiAssistantScreen(
    state: AiAssistantUiState,
    onSend: (String) -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
) {
    var prompt by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val canSend = prompt.isNotBlank() && !state.isSending
    val spacing = FintrackSpacingTokens.current
    val latestKey = state.messages.lastOrNull()?.id to state.isSending

    LaunchedEffect(latestKey) {
        val extraItems = if (state.isSending) 1 else 0
        val target = state.messages.lastIndex + extraItems
        if (target >= 0) {
            listState.animateScrollToItem(target)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FinTrack AI") },
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    AnimatedVisibility(visible = state.errorMessage != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = state.errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f),
                            )
                            Row {
                                TextButton(onClick = onRetry) { Text("Retry") }
                                TextButton(onClick = onDismissError) { Text("Dismiss") }
                            }
                        }
                    }
                    ChatComposer(
                        value = prompt,
                        onValueChange = { prompt = it },
                        enabled = !state.isSending,
                        onSend = {
                            if (canSend) {
                                onSend(prompt)
                                prompt = ""
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            items(state.messages, key = { it.id }) { message ->
                ChatBubble(message = message)
            }
            if (state.isSending) {
                item(key = "typing-indicator") {
                    TypingIndicatorBubble()
                }
            }
        }
    }
}

@Composable
fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    onSend: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isActive = enabled && value.isNotBlank()

    val containerColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        label = "chat-composer-container",
    )
    val sendBackground by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        label = "chat-composer-send-bg",
    )
    val sendIconTint by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "chat-composer-send-icon",
    )
    val borderBrush = Brush.linearGradient(
        colors = if (isFocused) {
            listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f),
            )
        } else {
            listOf(
                MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            )
        },
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(borderBrush, RoundedCornerShape(24.dp))
            .padding(1.dp)
            .clip(RoundedCornerShape(23.dp))
            .background(containerColor)
            .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            maxLines = 5,
            interactionSource = interactionSource,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 24.dp)
                .padding(horizontal = 2.dp, vertical = 10.dp),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = "Ask about spending, savings, or trends...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                innerTextField()
            },
        )

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = CircleShape,
            color = sendBackground,
            tonalElevation = if (isActive) 6.dp else 0.dp,
            shadowElevation = if (isActive) 4.dp else 0.dp,
        ) {
            IconButton(
                modifier = Modifier.size(42.dp),
                onClick = onSend,
                enabled = isActive,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Send",
                    tint = sendIconTint,
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: AiMessage,
) {
    val spacing = FintrackSpacingTokens.current
    val isUser = message.isUser
    val cleanedText = if (isUser) message.text else sanitizeAiText(message.text)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 8.dp,
                bottomEnd = if (isUser) 8.dp else 20.dp,
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            shadowElevation = if (isUser) 0.dp else 2.dp,
            tonalElevation = if (isUser) 0.dp else 1.dp,
            modifier = Modifier.fillMaxWidth(0.86f),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                if (!isUser) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "FinTrack AI",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                ParagraphText(
                    text = cleanedText,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = message.createdAt.formatConversationTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUser) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun ParagraphText(
    text: String,
    color: androidx.compose.ui.graphics.Color,
) {
    val paragraphs = remember(text) {
        text.split(Regex("\\n\\s*\\n")).map { it.trim() }.filter { it.isNotEmpty() }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (paragraphs.isEmpty()) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = color,
            )
        } else {
            paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyLarge,
                    color = color,
                )
            }
        }
    }
}

@Composable
private fun TypingIndicatorBubble() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                )
                Text(
                    text = "FinTrack AI is typing...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

internal fun sanitizeAiText(raw: String): String {
    return raw
        .replace(Regex("""\*{2,}"""), "")
        .replace("```", "")
        .lines()
        .joinToString("\n") { it.trimEnd() }
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
}

private fun LocalDateTime.formatConversationTime(): String {
    val hourValue = hour % 12
    val displayHour = if (hourValue == 0) 12 else hourValue
    val meridiem = if (hour < 12) "AM" else "PM"
    return "${displayHour}:${minute.toString().padStart(2, '0')} $meridiem"
}
