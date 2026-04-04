package com.app.fintrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.fintrack.presentation.navigation.FintrackRoot
import com.app.fintrack.presentation.theme.FintrackTheme
import com.app.fintrack.presentation.settings.ThemeSettingsViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: ThemeSettingsViewModel = koinViewModel()
            val themeMode = settingsViewModel.themeMode.collectAsStateWithLifecycle().value
            FintrackTheme(themeMode = themeMode) {
                FintrackRoot()
            }
        }
    }
}
