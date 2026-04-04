package com.app.fintrack.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

enum class TransactionType {
    INCOME,
    EXPENSE,
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class FinanceCategory(val title: String) {
    SALARY("Salary"),
    FOOD("Food"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    BILLS("Bills"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health"),
    TRAVEL("Travel"),
    SAVINGS("Savings"),
    INVESTMENT("Investment"),
    OTHER("Other"),
}

data class Transaction(
    val id: Long = 0L,
    val amount: Double,
    val type: TransactionType,
    val category: FinanceCategory,
    val date: LocalDate,
    val notes: String = "",
)

data class TransactionFilter(
    val searchQuery: String = "",
    val type: TransactionType? = null,
    val category: FinanceCategory? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)

data class WeeklySpendPoint(
    val label: String,
    val amount: Double,
)

data class SavingsGoal(
    val id: Long = 0L,
    val title: String,
    val targetAmount: Double,
    val currentSaved: Double,
)

data class DashboardSummary(
    val currentBalance: Double,
    val totalIncome: Double,
    val totalExpenses: Double,
    val monthToDateIncome: Double,
    val monthToDateExpenses: Double,
    val goal: SavingsGoal? = null,
    val weeklySpending: List<WeeklySpendPoint>,
    val recentTransactions: List<Transaction>,
)

data class CategorySpend(
    val category: FinanceCategory,
    val amount: Double,
)

data class InsightSummary(
    val totalIncome: Double,
    val totalExpenses: Double,
    val categoryBreakdown: List<CategorySpend>,
    val incomeTrend: List<WeeklySpendPoint>,
    val expenseTrend: List<WeeklySpendPoint>,
    val monthlyTrend: List<WeeklySpendPoint>,
    val highestSpendCategory: CategorySpend?,
    val thisWeekSpend: Double,
    val lastWeekSpend: Double,
)

data class AiMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val createdAt: LocalDateTime,
)
