package com.app.fintrack.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.fintrack.domain.model.DashboardSummary
import com.app.fintrack.domain.model.SavingsGoal
import com.app.fintrack.domain.usecase.AddSavingsGoalUseCase
import com.app.fintrack.domain.usecase.DeleteSavingsGoalUseCase
import com.app.fintrack.domain.usecase.ObserveBudgetLimitUseCase
import com.app.fintrack.domain.usecase.ObserveDashboardUseCase
import com.app.fintrack.domain.usecase.ObserveSavingsGoalsUseCase
import com.app.fintrack.domain.usecase.SetBudgetLimitUseCase
import com.app.fintrack.domain.usecase.UpdateSavingsGoalUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val summary: DashboardSummary? = null,
    val budgetLimit: Double = 0.0,
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val errorMessage: String? = null,
)

class HomeViewModel(
    private val observeDashboardUseCase: ObserveDashboardUseCase,
    private val observeBudgetLimitUseCase: ObserveBudgetLimitUseCase,
    private val setBudgetLimitUseCase: SetBudgetLimitUseCase,
    private val observeSavingsGoalsUseCase: ObserveSavingsGoalsUseCase,
    private val addSavingsGoalUseCase: AddSavingsGoalUseCase,
    private val updateSavingsGoalUseCase: UpdateSavingsGoalUseCase,
    private val deleteSavingsGoalUseCase: DeleteSavingsGoalUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeDashboardUseCase(),
                observeBudgetLimitUseCase(),
                observeSavingsGoalsUseCase(),
            ) { summary, budget, goals ->
                HomeUiState(
                    isLoading = false,
                    summary = summary,
                    budgetLimit = budget,
                    savingsGoals = goals,
                )
            }.collect { _uiState.value = it }
        }
    }

    fun updateBudgetLimit(value: String) {
        val amount = value.toDoubleOrNull()?.coerceAtLeast(0.0) ?: return
        viewModelScope.launch { setBudgetLimitUseCase(amount) }
    }

    fun addSavingsGoal(title: String, target: String, currentSaved: String) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty()) return
        val targetAmount = target.toDoubleOrNull()?.coerceAtLeast(1.0) ?: return
        val currentAmount = currentSaved.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        viewModelScope.launch {
            addSavingsGoalUseCase(
                SavingsGoal(
                    title = normalizedTitle,
                    targetAmount = targetAmount,
                    currentSaved = currentAmount,
                ),
            )
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch { deleteSavingsGoalUseCase(goal.id) }
    }

    fun addToSavingsGoal(goal: SavingsGoal, amount: String) {
        val addition = amount.toDoubleOrNull()?.coerceAtLeast(0.01) ?: return
        viewModelScope.launch {
            updateSavingsGoalUseCase(
                goal.copy(
                    currentSaved = (goal.currentSaved + addition).coerceAtMost(goal.targetAmount),
                ),
            )
        }
    }
}
