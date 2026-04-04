package com.app.fintrack.presentation.components

import com.app.fintrack.core.currentLocalDate
import com.app.fintrack.core.minusDaysSafe
import com.app.fintrack.domain.model.DashboardSummary
import com.app.fintrack.domain.model.FinanceCategory
import com.app.fintrack.domain.model.SavingsGoal
import com.app.fintrack.domain.model.Transaction
import com.app.fintrack.domain.model.TransactionType
import com.app.fintrack.domain.model.WeeklySpendPoint
import kotlinx.datetime.LocalDate

object PreviewFinanceData {
    private val today: LocalDate = currentLocalDate()

    val dashboard = DashboardSummary(
        currentBalance = 7340.0,
        totalIncome = 9800.0,
        totalExpenses = 2460.0,
        monthToDateIncome = 5200.0,
        monthToDateExpenses = 1240.0,
        weeklySpending = listOf(
            WeeklySpendPoint("Mon", 120.0),
            WeeklySpendPoint("Tue", 340.0),
            WeeklySpendPoint("Wed", 180.0),
            WeeklySpendPoint("Thu", 410.0),
            WeeklySpendPoint("Fri", 220.0),
            WeeklySpendPoint("Sat", 540.0),
            WeeklySpendPoint("Sun", 160.0),
        ),
        recentTransactions = listOf(
            Transaction(1, 250.0, TransactionType.EXPENSE, FinanceCategory.FOOD, today, "Lunch"),
            Transaction(2, 4800.0, TransactionType.INCOME, FinanceCategory.SALARY, today.minusDaysSafe(1), "Salary"),
        ),
    )
}
