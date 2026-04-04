package com.app.fintrack.presentation.ai

import com.app.fintrack.domain.model.AiMessage
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AiChatSessionStore {
    private val starter = AiMessage(
        id = UUID.randomUUID().toString(),
        text = "Ask things like 'Where did I spend most this month?'",
        isUser = false,
        createdAt = now(),
    )

    private val _messages = MutableStateFlow(listOf(starter))
    val messages: StateFlow<List<AiMessage>> = _messages.asStateFlow()

    fun append(message: AiMessage) {
        _messages.value = _messages.value + message
    }
}
