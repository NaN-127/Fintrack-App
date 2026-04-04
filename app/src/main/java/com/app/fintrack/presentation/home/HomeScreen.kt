package com.app.fintrack.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.fintrack.domain.model.SavingsGoal
import com.app.fintrack.domain.model.ThemeMode
import com.app.fintrack.presentation.components.AppSectionHeader
import com.app.fintrack.presentation.components.BalanceCard
import com.app.fintrack.presentation.components.ChartCard
import com.app.fintrack.presentation.components.CompactMetricCard
import com.app.fintrack.presentation.components.EmptyStateCard
import com.app.fintrack.presentation.components.LoadingCard
import com.app.fintrack.presentation.components.TransactionItem
import com.app.fintrack.presentation.settings.ThemeSettingsViewModel
import com.app.fintrack.presentation.theme.FintrackSpacingTokens
import com.app.fintrack.presentation.theme.FintrackTokens
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeRoute(
    onViewAllTransactions: () -> Unit,
    onAddTransaction: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    themeViewModel: ThemeSettingsViewModel = koinViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val themeMode = themeViewModel.themeMode.collectAsStateWithLifecycle().value
    HomeScreen(
        state = state,
        onViewAllTransactions = onViewAllTransactions,
        onAddTransaction = onAddTransaction,
        themeMode = themeMode,
        onToggleTheme = themeViewModel::cycleTheme,
        onUpdateBudget = viewModel::updateBudgetLimit,
        onAddGoal = viewModel::addSavingsGoal,
        onAddGoalProgress = viewModel::addToSavingsGoal,
        onDeleteGoal = viewModel::deleteSavingsGoal,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onViewAllTransactions: () -> Unit,
    onAddTransaction: () -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onUpdateBudget: (String) -> Unit,
    onAddGoal: (String, String, String) -> Unit,
    onAddGoalProgress: (SavingsGoal, String) -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit,
) {
    val spacing = FintrackSpacingTokens.current
    val visuals = FintrackTokens.visuals
    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by rememberSaveable { mutableStateOf(state.budgetLimit.toString()) }
    val budgetValid = budgetInput.toDoubleOrNull()?.let { it > 0 } == true
    var showGoalDialog by remember { mutableStateOf(false) }
    var goalTitleInput by rememberSaveable { mutableStateOf("") }
    var goalTargetInput by rememberSaveable { mutableStateOf("") }
    var goalCurrentSavedInput by rememberSaveable { mutableStateOf("") }
    val goalValid = goalTitleInput.isNotBlank() && goalTargetInput.toDoubleOrNull()?.let { it > 0 } == true
    var activeProgressGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var progressInput by rememberSaveable { mutableStateOf("") }
    val progressValid = progressInput.toDoubleOrNull()?.let { it > 0 } == true

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item {
            HomeTopBar(themeMode = themeMode, onToggleTheme = onToggleTheme)
        }
        when {
            state.isLoading -> item { LoadingCard() }
            state.errorMessage != null -> item { EmptyStateCard(state.errorMessage) }
            state.summary == null -> item { EmptyStateCard("No finance data yet. Add your first transaction to begin.") }
            else -> {
                val summary = state.summary
                item {
                    BalanceCard(
                        currentBalance = summary.currentBalance,
                        income = summary.totalIncome,
                        expenses = summary.totalExpenses,
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        CompactMetricCard(
                            title = "Monthly Income",
                            value = "$${"%,.0f".format(summary.monthToDateIncome)}",
                            modifier = Modifier.weight(1f),
                            highlight = visuals.success,
                        )
                        CompactMetricCard(
                            title = "Monthly Spend",
                            value = "$${"%,.0f".format(summary.monthToDateExpenses)}",
                            modifier = Modifier.weight(1f),
                            highlight = visuals.danger,
                        )
                    }
                }
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 3.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(spacing.md),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(text = "Monthly Budget", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "$${"%,.0f".format(summary.monthToDateExpenses)} of $${"%,.0f".format(state.budgetLimit)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                                    )
                                }
                                IconButton(onClick = {
                                    budgetInput = state.budgetLimit.toString()
                                    showBudgetDialog = true
                                }) {
                                    Icon(Icons.Outlined.Settings, contentDescription = "Edit budget")
                                }
                            }
                            val progress = if (state.budgetLimit > 0) {
                                (summary.monthToDateExpenses / state.budgetLimit).toFloat().coerceIn(0f, 1f)
                            } else {
                                0f
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress)
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(if (progress < 0.85f) visuals.success else visuals.danger),
                                )
                            }
                        }
                    }
                }
                item {
                    ChartCard(
                        title = "Weekly Spending",
                        subtitle = "Last 7 days",
                        points = summary.weeklySpending,
                        totalLabel = "$${"%,.0f".format(summary.weeklySpending.sumOf { it.amount })}",
                    )
                }
                item {
                    AppSectionHeader(
                        title = "Savings Goal",
                        actionLabel = "Set goal",
                        onAction = {
                            goalTitleInput = ""
                            goalTargetInput = ""
                            goalCurrentSavedInput = ""
                            showGoalDialog = true
                        },
                    )
                }
                item {
                    GoalsRow(
                        goals = state.savingsGoals,
                        onAddProgress = {
                            activeProgressGoal = it
                            progressInput = ""
                        },
                        onDeleteGoal = onDeleteGoal,
                    )
                }
                item {
                    AppSectionHeader(
                        title = "Recent Activity",
                        actionLabel = "See all",
                        onAction = onViewAllTransactions,
                    )
                }
                if (summary.recentTransactions.isEmpty()) {
                    item { EmptyStateCard("Transactions will appear here once you start logging them.") }
                } else {
                    items(summary.recentTransactions.take(4), key = { it.id }) { transaction ->
                        TransactionItem(transaction = transaction)
                    }
                }
            }
        }
    }

    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Set monthly limit") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    Text(
                        "Set your monthly spending cap for clearer progress tracking.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        singleLine = true,
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = budgetValid,
                    onClick = {
                        onUpdateBudget(budgetInput)
                        showBudgetDialog = false
                    },
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Create savings goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    OutlinedTextField(
                        value = goalTitleInput,
                        onValueChange = { goalTitleInput = it },
                        singleLine = true,
                        label = { Text("Goal title") },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    )
                    OutlinedTextField(
                        value = goalTargetInput,
                        onValueChange = { goalTargetInput = it },
                        singleLine = true,
                        label = { Text("Target amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    )
                    OutlinedTextField(
                        value = goalCurrentSavedInput,
                        onValueChange = { goalCurrentSavedInput = it },
                        singleLine = true,
                        label = { Text("Current saved (optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = goalValid,
                    onClick = {
                        onAddGoal(goalTitleInput, goalTargetInput, goalCurrentSavedInput)
                        showGoalDialog = false
                    },
                ) {
                    Text("Save Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (activeProgressGoal != null) {
        AlertDialog(
            onDismissRequest = { activeProgressGoal = null },
            title = { Text("Add savings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    Text(
                        text = "Update progress for ${activeProgressGoal?.title.orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                    OutlinedTextField(
                        value = progressInput,
                        onValueChange = { progressInput = it },
                        singleLine = true,
                        label = { Text("Amount to add") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = progressValid,
                    onClick = {
                        activeProgressGoal?.let { goal ->
                            onAddGoalProgress(goal, progressInput)
                        }
                        activeProgressGoal = null
                    },
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeProgressGoal = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun GoalsRow(
    goals: List<SavingsGoal>,
    onAddProgress: (SavingsGoal) -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FintrackSpacingTokens.current
    if (goals.isEmpty()) {
        EmptyStateCard(text = "Create a savings goal to start tracking progress.")
        return
    }
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        items(goals, key = { it.id }) { goal ->
            HomeGoalCard(
                goal = goal,
                onAddProgress = { onAddProgress(goal) },
                onDelete = { onDeleteGoal(goal) },
                modifier = Modifier.width(320.dp),
            )
        }
    }
}

@Composable
private fun HomeGoalCard(
    goal: SavingsGoal,
    onAddProgress: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FintrackSpacingTokens.current
    val progress = if (goal.targetAmount > 0) (goal.currentSaved / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val remaining = (goal.targetAmount - goal.currentSaved).coerceAtLeast(0.0)
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier.padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = goal.title, style = MaterialTheme.typography.titleMedium)
                Row {
                    TextButton(onClick = onAddProgress) {
                        Text("Add")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete savings goal",
                            tint = FintrackTokens.visuals.danger,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Saved $${"%,.0f".format(goal.currentSaved)}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Remaining $${"%,.0f".format(remaining)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
            )
            Text(
                text = "${(progress * 100).toInt()}% of $${"%,.0f".format(goal.targetAmount)} target",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
) {
    val spacing = FintrackSpacingTokens.current
    val visuals = FintrackTokens.visuals
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = visuals.avatarGradient,
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "N", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                Text(
                    text = "Welcome back, Nandu",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
        ) {
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (themeMode == ThemeMode.DARK) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = "Toggle theme",
                )
            }
        }
    }
}
