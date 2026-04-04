package com.app.fintrack.domain.usecase

import com.app.fintrack.domain.model.AiMessage
import com.app.fintrack.domain.model.DashboardSummary
import com.app.fintrack.domain.model.InsightSummary
import com.app.fintrack.domain.model.SavingsGoal
import com.app.fintrack.domain.model.ThemeMode
import com.app.fintrack.domain.model.Transaction
import com.app.fintrack.domain.model.TransactionFilter
import com.app.fintrack.domain.repository.AiRepository
import com.app.fintrack.domain.repository.FinanceRepository
import com.app.fintrack.domain.repository.SavingsGoalRepository
import com.app.fintrack.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class ObserveDashboardUseCase(private val repository: FinanceRepository) {
    operator fun invoke(): Flow<DashboardSummary> = repository.observeDashboardSummary()
}

class ObserveInsightsUseCase(private val repository: FinanceRepository) {
    operator fun invoke(): Flow<InsightSummary> = repository.observeInsightSummary()
}

class ObserveRecentTransactionsUseCase(private val repository: FinanceRepository) {
    operator fun invoke(limit: Int = 8): Flow<List<Transaction>> = repository.observeRecentTransactions(limit)
}

class ObserveTransactionsUseCase(private val repository: FinanceRepository) {
    operator fun invoke(filter: TransactionFilter): Flow<List<Transaction>> = repository.observeTransactions(filter)
}

class GetTransactionByIdUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke(id: Long): Transaction? = repository.getTransactionById(id)
}

class SaveTransactionUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke(transaction: Transaction) = repository.upsertTransaction(transaction)
}

class DeleteTransactionUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteTransaction(id)
}

class SeedFinanceDataUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke() = repository.seedIfEmpty()
}

class AskFinanceAssistantUseCase(private val repository: AiRepository) {
    suspend operator fun invoke(prompt: String): AiMessage = repository.sendMessage(prompt)
}

class ObserveThemeModeUseCase(private val repository: UserPreferencesRepository) {
    operator fun invoke(): Flow<ThemeMode> = repository.themeMode
}

class SetThemeModeUseCase(private val repository: UserPreferencesRepository) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}

class ObserveBudgetLimitUseCase(private val repository: UserPreferencesRepository) {
    operator fun invoke(): Flow<Double> = repository.budgetLimit
}

class SetBudgetLimitUseCase(private val repository: UserPreferencesRepository) {
    suspend operator fun invoke(amount: Double) = repository.setBudgetLimit(amount)
}

class ObserveSavingsGoalsUseCase(private val repository: SavingsGoalRepository) {
    operator fun invoke(): Flow<List<SavingsGoal>> = repository.observeSavingsGoals()
}

class AddSavingsGoalUseCase(private val repository: SavingsGoalRepository) {
    suspend operator fun invoke(goal: SavingsGoal) = repository.addSavingsGoal(goal)
}

class UpdateSavingsGoalUseCase(private val repository: SavingsGoalRepository) {
    suspend operator fun invoke(goal: SavingsGoal) = repository.updateSavingsGoal(goal)
}

class DeleteSavingsGoalUseCase(private val repository: SavingsGoalRepository) {
    suspend operator fun invoke(goalId: Long) = repository.deleteSavingsGoal(goalId)
}
