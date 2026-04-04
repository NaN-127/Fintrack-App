package com.app.fintrack.data.remote

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import com.app.fintrack.BuildConfig
import com.app.fintrack.core.currentLocalDateTime
import com.app.fintrack.domain.model.AiMessage
import com.app.fintrack.domain.model.Transaction
import io.ktor.client.HttpClient
import java.util.UUID
import kotlinx.datetime.LocalDateTime

interface FinanceAiRemoteDataSource {
    suspend fun askFinanceQuestion(prompt: String, transactions: List<Transaction>): AiMessage
}

class KoogGeminiFinanceAiRemoteDataSource(
    private val httpClient: HttpClient,
) : FinanceAiRemoteDataSource {

    override suspend fun askFinanceQuestion(prompt: String, transactions: List<Transaction>): AiMessage {
        val contextualPrompt = buildString {
            appendLine("You are a helpful personal finance companion for a mobile Android app.")
            appendLine("Reply in a concise, supportive way.")
            appendLine("Use the finance data below to answer the user request.")
            appendLine()
            appendLine("Recent transactions:")
            transactions.take(20).forEach { transaction ->
                appendLine(
                    "- ${transaction.date}: ${transaction.category.title} ${transaction.type.name.lowercase()} ${"%.2f".format(transaction.amount)} (${transaction.notes})"
                )
            }
            appendLine()
            appendLine("User request: $prompt")
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val responseText = if (apiKey.isBlank()) {
            localFallback(prompt = prompt, transactions = transactions, geminiConfigured = false)
        } else {
            runCatching {
                val agent = AIAgent(
                    promptExecutor = simpleGoogleAIExecutor(apiKey),
                    systemPrompt = "You are a finance assistant inside a mobile app. Keep answers practical and concise.",
                    llmModel = GoogleModels.Gemini2_5Flash,
                )
                agent.run(contextualPrompt)
            }.getOrElse {
                localFallback(prompt = prompt, transactions = transactions, geminiConfigured = true)
            }
        }

        return AiMessage(
            id = UUID.randomUUID().toString(),
            text = responseText,
            isUser = false,
            createdAt = nowLocalDateTime(),
        )
    }

    private fun localFallback(
        prompt: String,
        transactions: List<Transaction>,
        geminiConfigured: Boolean,
    ): String {
        val expenses = transactions.filter { it.type.name == "EXPENSE" }
        val topCategory = expenses
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
            .maxByOrNull { it.value }
        val totalSpent = expenses.sumOf { it.amount }

        return buildString {
            if (!geminiConfigured) {
                appendLine("Gemini is not configured yet, so here's a local finance summary.")
                appendLine("Add geminiApiKey to local.properties to enable live AI responses.")
                appendLine()
            }
            appendLine("Here is a quick finance summary for ${formatShortDate(nowLocalDateTime())}.")
            appendLine("You have tracked ${transactions.size} transactions and ${"%.0f".format(totalSpent)} in expenses.")
            if (topCategory != null) {
                appendLine("Your highest spend category is ${topCategory.key.title} at ${"%.0f".format(topCategory.value)}.")
            }
            if (prompt.contains("save", ignoreCase = true)) {
                append("A strong next step is to automate a fixed transfer into savings right after income arrives.")
            } else if (geminiConfigured) {
                append("Gemini was unavailable for this request, so I switched to a local summary.")
            }
        }.trim()
    }
}

private fun nowLocalDateTime(): LocalDateTime {
    return currentLocalDateTime()
}

private fun formatShortDate(value: LocalDateTime): String {
    val monthLabel = value.month.name.lowercase().replaceFirstChar(Char::titlecase).take(3)
    return "$monthLabel ${value.day}"
}
