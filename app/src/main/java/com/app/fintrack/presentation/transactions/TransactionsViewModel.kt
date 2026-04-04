package com.app.fintrack.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.fintrack.domain.model.FinanceCategory
import com.app.fintrack.domain.model.Transaction
import com.app.fintrack.domain.model.TransactionFilter
import com.app.fintrack.domain.model.TransactionType
import com.app.fintrack.domain.usecase.DeleteTransactionUseCase
import com.app.fintrack.domain.usecase.ObserveTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TransactionsUiState(
    val filter: TransactionFilter = TransactionFilter(),
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TransactionsViewModel(
    private val observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {
    private val filterFlow = MutableStateFlow(TransactionFilter())

    val uiState = filterFlow
        .flatMapLatest { filter ->
            observeTransactionsUseCase(filter).map { transactions ->
                TransactionsUiState(
                    filter = filter,
                    transactions = transactions,
                    isLoading = false,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState())

    fun onSearchChanged(value: String) {
        filterFlow.value = filterFlow.value.copy(searchQuery = value)
    }

    fun onTypeSelected(label: String?) {
        filterFlow.value = filterFlow.value.copy(
            type = when (label) {
                "Income" -> TransactionType.INCOME
                "Expense" -> TransactionType.EXPENSE
                else -> null
            },
        )
    }

    fun onCategorySelected(label: String?) {
        filterFlow.value = filterFlow.value.copy(
            category = FinanceCategory.entries.firstOrNull { it.title == label },
        )
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch { deleteTransactionUseCase(id) }
    }
}
