package com.app.fintrack.domain.repository

import com.app.fintrack.domain.model.AiMessage
import com.app.fintrack.domain.model.DashboardSummary
import com.app.fintrack.domain.model.InsightSummary
import com.app.fintrack.domain.model.SavingsGoal
import com.app.fintrack.domain.model.ThemeMode
import com.app.fintrack.domain.model.Transaction
import com.app.fintrack.domain.model.TransactionFilter
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    fun observeDashboardSummary(): Flow<DashboardSummary>
    fun observeInsightSummary(): Flow<InsightSummary>
    fun observeRecentTransactions(limit: Int): Flow<List<Transaction>>
    fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun upsertTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: Long)
    suspend fun seedIfEmpty()
}

interface AiRepository {
    suspend fun sendMessage(prompt: String): AiMessage
}

interface UserPreferencesRepository {
    val themeMode: Flow<ThemeMode>
    val budgetLimit: Flow<Double>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setBudgetLimit(amount: Double)
}

interface SavingsGoalRepository {
    fun observeSavingsGoals(): Flow<List<SavingsGoal>>
    suspend fun addSavingsGoal(goal: SavingsGoal)
    suspend fun updateSavingsGoal(goal: SavingsGoal)
    suspend fun deleteSavingsGoal(goalId: Long)
}
