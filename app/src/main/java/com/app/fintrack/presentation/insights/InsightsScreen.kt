package com.app.fintrack.presentation.insights

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.fintrack.presentation.components.CategoryBreakdownCard
import com.app.fintrack.presentation.components.ChartCard
import com.app.fintrack.presentation.components.CompactMetricCard
import com.app.fintrack.presentation.components.EmptyStateCard
import com.app.fintrack.presentation.components.LoadingCard
import com.app.fintrack.presentation.components.SegmentedSelector
import com.app.fintrack.presentation.theme.FintrackSpacingTokens
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun InsightsRoute(viewModel: InsightsViewModel = koinViewModel()) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    InsightsScreen(
        state = state,
        onModeSelected = viewModel::onModeSelected,
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun InsightsScreen(
    state: InsightsUiState,
    onModeSelected: (InsightsMode) -> Unit,
) {
    val spacing = FintrackSpacingTokens.current
    val modes = InsightsMode.entries
    val pagerState = rememberPagerState(
        initialPage = state.selectedMode.ordinal,
        pageCount = { modes.size },
    )
    val scope = rememberCoroutineScope()
    val activeMode by remember(pagerState, modes) {
        derivedStateOf { modes.getOrElse(pagerState.targetPage) { modes[pagerState.currentPage] } }
    }

    LaunchedEffect(state.selectedMode) {
        if (pagerState.currentPage != state.selectedMode.ordinal && pagerState.targetPage != state.selectedMode.ordinal) {
            pagerState.scrollToPage(state.selectedMode.ordinal)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .map { modes[it] }
            .distinctUntilChanged()
            .collectLatest { onModeSelected(it) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text(text = "Insights", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "Track how income and spending are moving over time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }
        }
        when {
            state.isLoading -> item { LoadingCard() }
            state.summary == null -> item { EmptyStateCard("Insights will appear after a bit of activity.") }
            else -> {
                val summary = state.summary
                item {
                    SegmentedSelector(
                        options = modes.map { it.title },
                        selected = activeMode.title,
                        onSelected = { label ->
                            val mode = modes.firstOrNull { it.title == label } ?: return@SegmentedSelector
                            if (mode.ordinal != pagerState.targetPage) {
                                scope.launch { pagerState.animateScrollToPage(mode.ordinal) }
                            }
                        },
                    )
                }
                item {
                    Box(modifier = Modifier.padding(top = 4.dp)) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth(),
                            beyondViewportPageCount = 0,
                            key = { modes[it].name },
                        ) { page ->
                            val mode = modes[page]
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.lg)) {
                                val points = if (mode == InsightsMode.INCOME) summary.incomeTrend else summary.expenseTrend
                                val total = if (mode == InsightsMode.INCOME) summary.totalIncome else summary.totalExpenses
                                val title = if (mode == InsightsMode.INCOME) "Income Flow" else "Expense Flow"
                                ChartCard(
                                    title = title,
                                    subtitle = "Last 7 days",
                                    points = points,
                                    totalLabel = "$${"%,.2f".format(total)}",
                                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
                                )
                                if (mode == InsightsMode.EXPENSE) {
                                    CategoryBreakdownCard(items = summary.categoryBreakdown)
                                } else {
                                    EmptyStateCard(text = "Income sources will appear once income transactions are logged.")
                                }
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        CompactMetricCard(
                            title = "Highest Spend",
                            value = summary.highestSpendCategory?.category?.title ?: "No data",
                            modifier = Modifier.weight(1f),
                        )
                        CompactMetricCard(
                            title = "Week vs Last",
                            value = buildString {
                                val change = summary.thisWeekSpend - summary.lastWeekSpend
                                append(if (change >= 0) "+" else "-")
                                append("$${"%,.0f".format(kotlin.math.abs(change))}")
                            },
                            modifier = Modifier.weight(1f),
                            highlight = if (summary.thisWeekSpend <= summary.lastWeekSpend) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        )
                    }
                }
                item {
                    ChartCard(
                        title = "Monthly Trend",
                        subtitle = "Last 5 months (expenses)",
                        points = summary.monthlyTrend,
                        totalLabel = "$${"%,.0f".format(summary.monthlyTrend.sumOf { it.amount })}",
                    )
                }
            }
        }
    }
}
