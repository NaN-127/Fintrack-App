package com.app.fintrack.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.fintrack.domain.usecase.ObserveInsightsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InsightsViewModel(
    private val observeInsightsUseCase: ObserveInsightsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeInsightsUseCase().collect {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        summary = it,
                    )
                }
            }
        }
    }

    fun onModeSelected(mode: InsightsMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }
}
