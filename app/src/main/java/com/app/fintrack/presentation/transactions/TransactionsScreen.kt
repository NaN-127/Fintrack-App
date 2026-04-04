package com.app.fintrack.presentation.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.fintrack.domain.model.FinanceCategory
import com.app.fintrack.presentation.components.DateHeader
import com.app.fintrack.presentation.components.EmptyStateCard
import com.app.fintrack.presentation.components.QuickFilters
import com.app.fintrack.presentation.components.TransactionItem
import com.app.fintrack.presentation.theme.FintrackSpacingTokens
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionsRoute(
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    viewModel: TransactionsViewModel = koinViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    TransactionsScreen(
        state = state,
        onSearchChanged = viewModel::onSearchChanged,
        onTypeSelected = viewModel::onTypeSelected,
        onCategorySelected = viewModel::onCategorySelected,
        onAddTransaction = onAddTransaction,
        onEditTransaction = onEditTransaction,
        onDeleteTransaction = viewModel::deleteTransaction,
    )
}

@Composable
fun TransactionsScreen(
    state: TransactionsUiState,
    onSearchChanged: (String) -> Unit,
    onTypeSelected: (String?) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
) {
    val pendingDeleteId = remember { mutableStateOf<Long?>(null) }
    val spacing = FintrackSpacingTokens.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Outlined.Add, contentDescription = "Add transaction")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Text("Transactions", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Search, filter, and manage every money movement in one place.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = state.filter.searchQuery,
                    onValueChange = onSearchChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    label = { Text("Search notes or category") },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text("Quick filters", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        QuickFilters(
                            options = listOf("All", "Income", "Expense"),
                            selected = state.filter.type?.name?.lowercase()?.replaceFirstChar { it.titlecase() } ?: "All",
                            onSelected = onTypeSelected,
                        )
                    }
                    QuickFilters(
                        options = listOf("All") + FinanceCategory.entries.map { it.title },
                        selected = state.filter.category?.title ?: "All",
                        onSelected = { label -> onCategorySelected(label?.takeIf { it != "All" }) },
                    )
                }
            }
            when {
                state.isLoading -> item { EmptyStateCard("Loading transactions...") }
                state.transactions.isEmpty() -> item { EmptyStateCard("No transactions match the active filters.") }
                else -> itemsIndexed(state.transactions, key = { _, item -> item.id }) { index, item ->
                    val previous = state.transactions.getOrNull(index - 1)
                    val showHeader = previous?.date != item.date
                    if (showHeader) {
                        DateHeader(date = item.date)
                    }
                    Card(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        ),
                    ) {
                        Column(modifier = Modifier.padding(spacing.xs)) {
                            TransactionItem(transaction = item, onClick = { onEditTransaction(item.id) })
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                IconButton(onClick = { pendingDeleteId.value = item.id }) {
                                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDeleteId.value?.let { transactionId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId.value = null },
            title = { Text("Delete transaction?") },
            text = { Text("This action removes the transaction from your finance history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteId.value = null
                        onDeleteTransaction(transactionId)
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId.value = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}
