package com.app.fintrack.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.fintrack.core.currentLocalDate
import com.app.fintrack.domain.model.FinanceCategory
import com.app.fintrack.domain.model.Transaction
import com.app.fintrack.domain.model.TransactionType
import com.app.fintrack.domain.usecase.GetTransactionByIdUseCase
import com.app.fintrack.domain.usecase.SaveTransactionUseCase
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class TransactionFormUiState(
    val isLoading: Boolean = false,
    val existingTransaction: Transaction? = null,
)

class TransactionFormViewModel(
    transactionId: Long?,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionFormUiState(isLoading = transactionId != null))
    val uiState = _uiState.asStateFlow()

    init {
        if (transactionId != null) {
            viewModelScope.launch {
                _uiState.value = TransactionFormUiState(
                    isLoading = false,
                    existingTransaction = getTransactionByIdUseCase(transactionId),
                )
            }
        }
    }

    fun saveTransaction(
        id: Long,
        amount: String,
        type: TransactionType,
        category: FinanceCategory,
        date: String,
        notes: String,
        onSaved: () -> Unit,
    ) {
        val parsedAmount = amount.toDoubleOrNull()?.let { abs(it) }?.takeIf { it > 0.0 } ?: return
        val parsedDate = runCatching { LocalDate.parse(date) }
            .getOrElse { currentLocalDate() }
        viewModelScope.launch {
            saveTransactionUseCase(
                Transaction(
                    id = id,
                    amount = parsedAmount,
                    type = type,
                    category = category,
                    date = parsedDate,
                    notes = notes,
                ),
            )
            onSaved()
        }
    }
}
