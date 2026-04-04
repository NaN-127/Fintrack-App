package com.app.fintrack.presentation.insights

import com.app.fintrack.domain.model.InsightSummary

enum class InsightsMode(val title: String) {
    INCOME("Income"),
    EXPENSE("Expense"),
}

data class InsightsUiState(
    val isLoading: Boolean = true,
    val summary: InsightSummary? = null,
    val selectedMode: InsightsMode = InsightsMode.INCOME,
)
