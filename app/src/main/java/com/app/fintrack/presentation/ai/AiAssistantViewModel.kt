package com.app.fintrack.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.fintrack.core.currentLocalDateTime
import com.app.fintrack.domain.model.AiMessage
import com.app.fintrack.domain.model.DashboardSummary
import com.app.fintrack.domain.model.InsightSummary
import com.app.fintrack.domain.usecase.AskFinanceAssistantUseCase
import com.app.fintrack.domain.usecase.ObserveDashboardUseCase
import com.app.fintrack.domain.usecase.ObserveInsightsUseCase
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

data class AiAssistantUiState(
    val messages: List<AiMessage> = emptyList(),
    val dashboard: DashboardSummary? = null,
    val insights: InsightSummary? = null,
    val isSending: Boolean = false,
    val lastPrompt: String? = null,
    val errorMessage: String? = null,
)

class AiAssistantViewModel(
    private val askFinanceAssistantUseCase: AskFinanceAssistantUseCase,
    private val observeDashboardUseCase: ObserveDashboardUseCase,
    private val observeInsightsUseCase: ObserveInsightsUseCase,
    private val sessionStore: AiChatSessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantUiState(messages = sessionStore.messages.value))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeDashboardUseCase(),
                observeInsightsUseCase(),
                sessionStore.messages,
            ) { dashboard, insights, messages ->
                Triple(dashboard, insights, messages)
            }.collect { (dashboard, insights, messages) ->
                _uiState.update {
                    it.copy(
                        messages = messages,
                        dashboard = dashboard,
                        insights = insights,
                    )
                }
            }
        }
    }

    fun send(prompt: String) {
        val normalized = prompt.trim()
        if (normalized.isBlank() || _uiState.value.isSending) return

        val userMessage = AiMessage(
            id = UUID.randomUUID().toString(),
            text = normalized,
            isUser = true,
            createdAt = now(),
        )

        sessionStore.append(userMessage)
        _uiState.update {
            it.copy(
                isSending = true,
                lastPrompt = normalized,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            runCatching {
                askFinanceAssistantUseCase(normalized)
            }.onSuccess { reply ->
                sessionStore.append(
                    reply.copy(
                        text = sanitizeAiText(reply.text),
                        createdAt = now(),
                    ),
                )
                _uiState.update { it.copy(isSending = false) }
            }.onFailure {
                sessionStore.append(errorMessage())
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = "We couldn't reach FinTrack AI",
                    )
                }
            }
        }
    }

    fun retryLast() {
        _uiState.value.lastPrompt?.let(::send)
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun errorMessage() = AiMessage(
        id = UUID.randomUUID().toString(),
        text = "Something went wrong. Check your Gemini key and try again.",
        isUser = false,
        createdAt = now(),
    )
}

internal fun now(): LocalDateTime = currentLocalDateTime()
