package com.app.fintrack.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.fintrack.domain.model.ThemeMode
import com.app.fintrack.data.local.SavingsGoalDao
import com.app.fintrack.data.local.SavingsGoalEntity
import com.app.fintrack.domain.model.SavingsGoal
import com.app.fintrack.domain.repository.SavingsGoalRepository
import com.app.fintrack.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val budgetKey = doublePreferencesKey("budget_limit")

    override val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val raw = preferences[themeKey] ?: ThemeMode.SYSTEM.name
        ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.SYSTEM
    }

    override val budgetLimit: Flow<Double> = dataStore.data.map { preferences ->
        preferences[budgetKey] ?: 0.0
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[themeKey] = mode.name }
    }

    override suspend fun setBudgetLimit(amount: Double) {
        dataStore.edit { it[budgetKey] = amount }
    }
}

class SavingsGoalRepositoryImpl(
    private val savingsGoalDao: SavingsGoalDao,
) : SavingsGoalRepository {
    override fun observeSavingsGoals(): Flow<List<SavingsGoal>> {
        return savingsGoalDao.observeAllGoals().map { goals ->
            goals.map(SavingsGoalEntity::toDomain)
        }
    }

    override suspend fun addSavingsGoal(goal: SavingsGoal) {
        savingsGoalDao.upsert(goal.toEntity())
    }

    override suspend fun updateSavingsGoal(goal: SavingsGoal) {
        savingsGoalDao.upsert(goal.toEntity())
    }

    override suspend fun deleteSavingsGoal(goalId: Long) {
        savingsGoalDao.deleteById(goalId)
    }
}

private fun SavingsGoalEntity.toDomain(): SavingsGoal = SavingsGoal(
    id = id,
    title = title,
    targetAmount = targetAmount,
    currentSaved = currentSaved,
)

private fun SavingsGoal.toEntity(): SavingsGoalEntity = SavingsGoalEntity(
    id = id,
    title = title,
    targetAmount = targetAmount,
    currentSaved = currentSaved,
)
