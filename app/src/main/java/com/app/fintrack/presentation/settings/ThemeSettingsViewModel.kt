package com.app.fintrack.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.fintrack.domain.model.ThemeMode
import com.app.fintrack.domain.usecase.ObserveThemeModeUseCase
import com.app.fintrack.domain.usecase.SetThemeModeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeSettingsViewModel(
    observeThemeModeUseCase: ObserveThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
) : ViewModel() {
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode = _themeMode.asStateFlow()

    init {
        viewModelScope.launch {
            observeThemeModeUseCase().collect { mode ->
                _themeMode.value = mode
            }
        }
    }

    fun cycleTheme() {
        val next = when (_themeMode.value) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            else -> ThemeMode.DARK
        }
        _themeMode.value = next
        viewModelScope.launch { setThemeModeUseCase(next) }
    }
}
