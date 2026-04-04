package com.app.fintrack.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.fintrack.domain.model.SavingsGoal
import com.app.fintrack.domain.usecase.ObserveSavingsGoalsUseCase
import com.app.fintrack.domain.usecase.UpdateSavingsGoalUseCase
import kotlin.math.min
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GoalsUiState(
    val goals: List<SavingsGoal> = emptyList(),
)

class GoalsViewModel(
    observeSavingsGoalsUseCase: ObserveSavingsGoalsUseCase,
    private val updateSavingsGoalUseCase: UpdateSavingsGoalUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeSavingsGoalsUseCase().collect {
                _uiState.value = GoalsUiState(goals = it)
            }
        }
    }

    fun addProgress(goal: SavingsGoal, amount: String) {
        val parsedAmount = amount.toDoubleOrNull()?.coerceAtLeast(0.01) ?: return
        viewModelScope.launch {
            updateSavingsGoalUseCase(
                goal.copy(currentSaved = min(goal.currentSaved + parsedAmount, goal.targetAmount)),
            )
        }
    }
}
