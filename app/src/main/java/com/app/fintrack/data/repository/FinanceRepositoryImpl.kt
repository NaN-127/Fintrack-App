package com.app.fintrack.data.repository

import com.app.fintrack.core.atStartOfMonth
import com.app.fintrack.core.atStartOfWeekMonday
import com.app.fintrack.core.currentLocalDate
import com.app.fintrack.core.minusDaysSafe
import com.app.fintrack.core.minusMonthsSafe
import com.app.fintrack.core.plusMonthsSafe
import com.app.fintrack.data.local.TransactionDao
import com.app.fintrack.data.local.TransactionEntity
import com.app.fintrack.data.remote.FinanceAiRemoteDataSource
import com.app.fintrack.domain.model.AiMessage
import com.app.fintrack.domain.model.CategorySpend
import com.app.fintrack.domain.model.DashboardSummary
import com.app.fintrack.domain.model.InsightSummary
import com.app.fintrack.domain.model.SavingsGoal
import com.app.fintrack.domain.model.Transaction
import com.app.fintrack.domain.model.TransactionFilter
import com.app.fintrack.domain.model.TransactionType
import com.app.fintrack.domain.model.WeeklySpendPoint
import com.app.fintrack.domain.repository.AiRepository
import com.app.fintrack.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class FinanceRepositoryImpl(
    private val transactionDao: TransactionDao,
) : FinanceRepository {

    override fun observeDashboardSummary(): Flow<DashboardSummary> {
        return transactionDao.observeAllTransactions().map { entities ->
            val transactions = entities.map(TransactionEntity::toDomain)
            val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val today = currentLocalDate()
            val monthStart = LocalDate(today.year, today.month, 1)
            val monthIncome = transactions
                .filter { it.type == TransactionType.INCOME && it.date >= monthStart && it.date <= today }
                .sumOf { it.amount }
            val monthExpenses = transactions
                .filter { it.type == TransactionType.EXPENSE && it.date >= monthStart && it.date <= today }
                .sumOf { it.amount }
            DashboardSummary(
                currentBalance = income - expenses,
                totalIncome = income,
                totalExpenses = expenses,
                monthToDateIncome = monthIncome,
                monthToDateExpenses = monthExpenses,
                weeklySpending = weeklySpending(transactions),
                recentTransactions = transactions
                    .sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id })
                    .take(8),
            )
        }
    }

    override fun observeInsightSummary(): Flow<InsightSummary> {
        return transactionDao.observeAllTransactions().map { entities ->
            val transactions = entities.map(TransactionEntity::toDomain)
            val income = transactions.filter { it.type == TransactionType.INCOME }
            val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
            val categoryBreakdown = expenses
                .groupBy { it.category }
                .map { (category, items) -> CategorySpend(category, items.sumOf { it.amount }) }
                .sortedByDescending { it.amount }
            val today = currentLocalDate()
            val thisWeekStart = today.atStartOfWeekMonday()
            val lastWeekStart = thisWeekStart.minusDaysSafe(7)
            val lastWeekEnd = thisWeekStart.minusDaysSafe(1)

            InsightSummary(
                totalIncome = income.sumOf { it.amount },
                totalExpenses = expenses.sumOf { it.amount },
                categoryBreakdown = categoryBreakdown,
                incomeTrend = weeklyAmounts(transactions, TransactionType.INCOME),
                expenseTrend = weeklyAmounts(transactions, TransactionType.EXPENSE),
                monthlyTrend = monthlyAmounts(transactions, TransactionType.EXPENSE),
                highestSpendCategory = categoryBreakdown.firstOrNull(),
                thisWeekSpend = expenses.filter { it.date >= thisWeekStart }.sumOf { it.amount },
                lastWeekSpend = expenses.filter { it.date in lastWeekStart..lastWeekEnd }.sumOf { it.amount },
            )
        }
    }

    override fun observeRecentTransactions(limit: Int): Flow<List<Transaction>> {
        return transactionDao.observeRecentTransactions(limit).map { entities ->
            entities.map(TransactionEntity::toDomain)
        }
    }

    override fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>> {
        return transactionDao.observeFilteredTransactions(
            searchQuery = filter.searchQuery.trim(),
            type = filter.type,
            category = filter.category,
            startDate = filter.startDate,
            endDate = filter.endDate,
        ).map { entities ->
            entities.map(TransactionEntity::toDomain)
        }
    }

    override suspend fun getTransactionById(id: Long): Transaction? = transactionDao.getById(id)?.toDomain()

    override suspend fun upsertTransaction(transaction: Transaction) {
        transactionDao.upsert(transaction.toEntity())
    }

    override suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteById(id)
    }

    override suspend fun seedIfEmpty() {

    }

    private fun weeklySpending(transactions: List<Transaction>): List<WeeklySpendPoint> {
        return weeklyAmounts(transactions, TransactionType.EXPENSE)
    }

    private fun weeklyAmounts(
        transactions: List<Transaction>,
        type: TransactionType,
    ): List<WeeklySpendPoint> {
        val today = currentLocalDate()
        return (6 downTo 0).map { offset ->
            val date = today.minusDaysSafe(offset)
            val amount = transactions
                .filter { it.type == type && it.date == date }
                .sumOf { it.amount }
            WeeklySpendPoint(label = date.dayOfWeek.name.take(3), amount = amount)
        }
    }

    private fun monthlyAmounts(
        transactions: List<Transaction>,
        type: TransactionType,
    ): List<WeeklySpendPoint> {
        val today = currentLocalDate()
        return (4 downTo 0).map { offset ->
            val monthStart = today.minusMonthsSafe(offset).atStartOfMonth()
            val monthEnd = monthStart.plusMonthsSafe(1).minusDaysSafe(1)
            val amount = transactions
                .filter { it.type == type && it.date in monthStart..monthEnd }
                .sumOf { it.amount }
            WeeklySpendPoint(label = monthStart.month.name.take(3), amount = amount)
        }
    }
}

class AiRepositoryImpl(
    private val remoteDataSource: FinanceAiRemoteDataSource,
    private val financeRepository: FinanceRepository,
) : AiRepository {
    override suspend fun sendMessage(prompt: String): AiMessage {
        val transactions = financeRepository.observeRecentTransactions(limit = 20).first()
        return remoteDataSource.askFinanceQuestion(prompt, transactions)
    }
}

private fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    amount = amount,
    type = type,
    category = category,
    date = date,
    notes = notes,
)

private fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amount = amount,
    type = type,
    category = category,
    date = date,
    notes = notes,
)
