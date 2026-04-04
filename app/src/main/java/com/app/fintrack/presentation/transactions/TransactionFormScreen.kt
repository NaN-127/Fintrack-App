package com.app.fintrack.presentation.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.fintrack.core.currentLocalDate
import com.app.fintrack.domain.model.FinanceCategory
import com.app.fintrack.domain.model.TransactionType
import com.app.fintrack.presentation.theme.FintrackSpacingTokens
import kotlinx.datetime.LocalDate
import kotlin.math.abs
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TransactionFormRoute(transactionId: Long?, onSaved: () -> Unit, onBack: () -> Unit) {
    val viewModel: TransactionFormViewModel = koinViewModel(parameters = { parametersOf(transactionId) })
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    TransactionFormScreen(state = state, onBack = onBack, onSaved = onSaved, onSave = viewModel::saveTransaction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    state: TransactionFormUiState,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onSave: (Long, String, TransactionType, FinanceCategory, String, String, () -> Unit) -> Unit,
) {
    var amount by rememberSaveable(state.existingTransaction?.id) { mutableStateOf(state.existingTransaction?.amount?.toString().orEmpty()) }
    var type by rememberSaveable(state.existingTransaction?.id) { mutableStateOf(state.existingTransaction?.type ?: TransactionType.EXPENSE) }
    var category by rememberSaveable(state.existingTransaction?.id) { mutableStateOf(state.existingTransaction?.category ?: FinanceCategory.FOOD) }
    var date by rememberSaveable(state.existingTransaction?.id) {
        mutableStateOf(state.existingTransaction?.date?.toString() ?: currentDate().toString())
    }
    var notes by rememberSaveable(state.existingTransaction?.id) { mutableStateOf(state.existingTransaction?.notes.orEmpty()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }
    val spacing = FintrackSpacingTokens.current
    val amountValid = amount.toDoubleOrNull()?.let { abs(it) > 0.0 } == true
    val dateValid = runCatching { LocalDate.parse(date) }.isSuccess
    val notesValid = notes.isNotBlank()
    val isFormValid = amountValid && dateValid && notesValid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.existingTransaction == null) "Add transaction" else "Edit transaction") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (!isFormValid) {
                                showErrors = true
                                return@TextButton
                            }
                            onSave(state.existingTransaction?.id ?: 0L, amount, type, category, date, notes, onSaved)
                        },
                    ) {
                        Text("Save")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount") },
                singleLine = true,
                isError = showErrors && !amountValid,
                supportingText = if (showErrors && !amountValid) {
                    { Text("Enter an amount greater than 0.") }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
            )
            ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                OutlinedTextField(
                    value = type.name.lowercase().replaceFirstChar { it.titlecase() },
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    label = { Text("Type") },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                )
                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    TransactionType.entries.forEach { item ->
                        DropdownMenuItem(text = { Text(item.name.lowercase().replaceFirstChar { it.titlecase() }) }, onClick = {
                            type = item
                            typeExpanded = false
                        })
                    }
                }
            }
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                OutlinedTextField(
                    value = category.title,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    label = { Text("Category") },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                )
                DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    FinanceCategory.entries.forEach { item ->
                        DropdownMenuItem(text = { Text(item.title) }, onClick = {
                            category = item
                            categoryExpanded = false
                        })
                    }
                }
            }
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date (YYYY-MM-DD)") },
                singleLine = true,
                isError = showErrors && !dateValid,
                supportingText = if (showErrors && !dateValid) {
                    { Text("Use the format YYYY-MM-DD.") }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes") },
                minLines = 4,
                isError = showErrors && !notesValid,
                supportingText = if (showErrors && !notesValid) {
                    { Text("Add a short note so you can find it later.") }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
            )
        }
    }
}

private fun currentDate(): LocalDate = currentLocalDate()
