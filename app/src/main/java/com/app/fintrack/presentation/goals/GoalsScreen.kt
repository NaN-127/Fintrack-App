package com.app.fintrack.presentation.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.fintrack.domain.model.SavingsGoal
import com.app.fintrack.presentation.components.EmptyStateCard
import com.app.fintrack.presentation.components.GoalProgressCard
import com.app.fintrack.presentation.theme.FintrackSpacingTokens
import org.koin.androidx.compose.koinViewModel

@Composable
fun GoalsRoute(viewModel: GoalsViewModel = koinViewModel()) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    GoalsScreen(
        state = state,
        onAddProgress = viewModel::addProgress,
    )
}

@Composable
fun GoalsScreen(
    state: GoalsUiState,
    onAddProgress: (SavingsGoal, String) -> Unit,
) {
    val spacing = FintrackSpacingTokens.current
    val progressDrafts = remember { mutableStateMapOf<Long, String>() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text("Savings Goals", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Track progress and add savings anytime. Updates are stored locally and reflected instantly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (state.goals.isEmpty()) {
            item { EmptyStateCard("No goal yet. Create one from Home to start tracking progress.") }
        } else {
            items(state.goals, key = { it.id }) { goal ->
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    GoalProgressCard(goal = goal)
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = progressDrafts[goal.id].orEmpty(),
                        onValueChange = { progressDrafts[goal.id] = it },
                        singleLine = true,
                        label = { Text("Add progress") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onAddProgress(goal, progressDrafts[goal.id].orEmpty())
                            progressDrafts[goal.id] = ""
                        },
                    ) {
                        Text("Update progress")
                    }
                }
            }
        }
        item {
            EmptyStateCard("Savings goals are stored in the local database and update instantly across Home and Goals.")
        }
    }
}
