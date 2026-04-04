package com.app.fintrack.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.fintrack.domain.model.CategorySpend
import com.app.fintrack.domain.model.FinanceCategory
import com.app.fintrack.domain.model.SavingsGoal
import com.app.fintrack.domain.model.Transaction
import com.app.fintrack.domain.model.TransactionType
import com.app.fintrack.domain.model.WeeklySpendPoint
import com.app.fintrack.presentation.theme.FintrackSpacingTokens
import com.app.fintrack.presentation.theme.FintrackTokens
import kotlinx.datetime.LocalDate
import kotlin.math.roundToInt

@Composable
fun AppSectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val spacing = FintrackSpacingTokens.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = spacing.sm, vertical = spacing.xs),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun BalanceCard(
    currentBalance: Double,
    income: Double,
    expenses: Double,
    modifier: Modifier = Modifier,
) {
    val visuals = FintrackTokens.visuals
    val spacing = FintrackSpacingTokens.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                20.dp,
                RoundedCornerShape(28.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            )
            .clip(RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(28.dp),
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(visuals.primaryGradient),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Text(
                    text = "Your Balance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = "$${"%,.2f".format(currentBalance)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    HeroMetric(
                        label = "Total Income",
                        amount = income,
                        modifier = Modifier.weight(1f),
                    )
                    HeroMetric(
                        label = "Total Expense",
                        amount = expenses,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetric(
    label: String,
    amount: Double,
    modifier: Modifier = Modifier,
) {
    val spacing = FintrackSpacingTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = label, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "$${"%,.2f".format(amount)}",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun GoalProgressCard(goal: SavingsGoal, modifier: Modifier = Modifier) {
    val progress = (goal.currentSaved / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
    val spacing = FintrackSpacingTokens.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(text = goal.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "$${"%,.2f".format(goal.currentSaved)} / $${"%,.2f".format(goal.targetAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                }
                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

@Composable
fun ChartCard(
    title: String,
    subtitle: String,
    points: List<WeeklySpendPoint>,
    totalLabel: String? = null,
    modifier: Modifier = Modifier,
) {
    val spacing = FintrackSpacingTokens.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (totalLabel != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Text(
                            text = totalLabel,
                            modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            WeeklyBarChart(points = points)
        }
    }
}

@Composable
fun WeeklyBarChart(
    points: List<WeeklySpendPoint>,
    modifier: Modifier = Modifier,
) {
    val max = (points.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(1.0)
    val visuals = FintrackTokens.visuals
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        points.forEach { point ->
            val normalized = when {
                point.amount <= 0.0 -> 0f
                else -> (point.amount / max).toFloat().coerceIn(0.1f, 1f)
            }
            val animated by animateFloatAsState(
                targetValue = normalized,
                animationSpec = tween(450, easing = FastOutSlowInEasing),
                label = "weekly-bar",
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (point.amount > 0) "$${"%.0f".format(point.amount)}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                Box(
                    modifier = Modifier
                        .size(width = 28.dp, height = 120.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    if (animated > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp * animated)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Brush.verticalGradient(visuals.chartGradient)),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = point.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val positive = transaction.type == TransactionType.INCOME
    val categoryIcon = transaction.category.icon(transaction.type)
    val visuals = FintrackTokens.visuals
    val spacing = FintrackSpacingTokens.current
    val amountColor = if (positive) visuals.success else visuals.danger
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(
                            if (positive) {
                                listOf(visuals.successContainer, visuals.success.copy(alpha = 0.24f))
                            } else {
                                listOf(visuals.infoContainer, MaterialTheme.colorScheme.secondaryContainer)
                            },
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = transaction.category.title,
                    tint = if (positive) visuals.success else MaterialTheme.colorScheme.primary,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Text(
                    text = transaction.notes.ifBlank { transaction.category.title },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (positive) "Money in" else transaction.category.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = transaction.date.formatLongDate(),
                        modifier = Modifier.padding(horizontal = spacing.sm, vertical = 5.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text(
                    text = "${if (positive) "+" else "-"}$${"%,.2f".format(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = amountColor,
                )
                Surface(
                    color = amountColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = if (positive) "Income" else "Expense",
                        modifier = Modifier.padding(horizontal = spacing.sm, vertical = 5.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = amountColor,
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownCard(
    items: List<CategorySpend>,
    modifier: Modifier = Modifier,
) {
    val total = items.sumOf { it.amount }.coerceAtLeast(1.0)
    val visuals = FintrackTokens.visuals
    val spacing = FintrackSpacingTokens.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Text(text = "Spending by Category", style = MaterialTheme.typography.titleMedium)
            if (items.isEmpty()) {
                EmptyStateCard(text = "Add a few expenses to unlock category insights.")
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CategoryDonutChart(
                        items = items.take(visuals.categoryPalette.size),
                        modifier = Modifier.size(116.dp),
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        Text(
                            text = "Total ${"$"}${"%,.0f".format(total)}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        items.take(visuals.categoryPalette.size).forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(visuals.categoryPalette[index], CircleShape),
                                    )
                                    Text(text = item.category.title, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(
                                    text = "$${"%,.0f".format(item.amount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDonutChart(
    items: List<CategorySpend>,
    modifier: Modifier = Modifier,
) {
    val total = items.sumOf { it.amount }.coerceAtLeast(1.0)
    val visuals = FintrackTokens.visuals
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.16f
        var startAngle = -90f
        items.forEachIndexed { index, item ->
            val sweep = ((item.amount / total) * 360f).toFloat()
            drawArc(
                color = visuals.categoryPalette[index % visuals.categoryPalette.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(strokeWidth, strokeWidth),
                size = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            startAngle += sweep
        }
    }
}

@Composable
fun StackedCategoryBar(
    items: List<CategorySpend>,
    modifier: Modifier = Modifier,
) {
    val total = items.sumOf { it.amount }.coerceAtLeast(1.0)
    val visuals = FintrackTokens.visuals
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        items.take(visuals.categoryPalette.size).forEachIndexed { index, item ->
            Box(
                modifier = Modifier
                    .weight((item.amount / total).toFloat())
                    .height(12.dp)
                    .background(visuals.categoryPalette[index]),
            )
        }
    }
}

@Composable
fun SegmentedSelector(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 1.dp,
    ) {
        val spacing = FintrackSpacingTokens.current
        Row(modifier = Modifier.padding(spacing.xs), horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            options.forEach { option ->
                val isSelected = option == selected
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
                    } else {
                        Color.Transparent
                    },
                    animationSpec = tween(durationMillis = 220),
                    label = "segment-container",
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = tween(durationMillis = 220),
                    label = "segment-text",
                )
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.outline.copy(alpha = 0.28f) else Color.Transparent,
                            shape = RoundedCornerShape(999.dp),
                        )
                        .clickable { onSelected(option) },
                    color = containerColor,
                    shape = RoundedCornerShape(999.dp),
                    tonalElevation = if (isSelected) 1.dp else 0.dp,
                    shadowElevation = if (isSelected) 1.dp else 0.dp,
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.padding(
                            horizontal = spacing.sm + 2.dp,
                            vertical = spacing.sm + 2.dp,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor,
                    )
                }
            }
        }
    }
}

@Composable
fun CompactMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Color = MaterialTheme.colorScheme.primary,
) {
    val spacing = FintrackSpacingTokens.current
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier.padding(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = highlight,
            )
        }
    }
}

@Composable
fun QuickFilters(
    options: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
) {
    val spacing = FintrackSpacingTokens.current
    LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
        items(options) { option ->
            AssistChip(
                onClick = { onSelected(if (selected == option) null else option) },
                label = { Text(text = option) },
            )
        }
    }
}

@Composable
fun LoadingCard(modifier: Modifier = Modifier) {
    val spacing = FintrackSpacingTokens.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeCap = StrokeCap.Round)
            Text(text = "Loading your finance snapshot...")
        }
    }
}

@Composable
fun EmptyStateCard(text: String, modifier: Modifier = Modifier) {
    val spacing = FintrackSpacingTokens.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(spacing.md),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun DateHeader(date: LocalDate, modifier: Modifier = Modifier) {
    val spacing = FintrackSpacingTokens.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(999.dp),
        tonalElevation = 1.dp,
    ) {
        Text(
            text = date.formatLongDate(),
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.xs),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun FinanceCategory.icon(type: TransactionType): ImageVector {
    if (type == TransactionType.INCOME) {
        return when (this) {
            FinanceCategory.SALARY -> Icons.Outlined.AttachMoney
            FinanceCategory.INVESTMENT -> Icons.AutoMirrored.Outlined.TrendingUp
            FinanceCategory.SAVINGS -> Icons.Outlined.Savings
            else -> Icons.Outlined.Payments
        }
    }
    return when (this) {
        FinanceCategory.SALARY -> Icons.Outlined.AttachMoney
        FinanceCategory.FOOD -> Icons.Outlined.Restaurant
        FinanceCategory.TRANSPORT -> Icons.Outlined.DirectionsBus
        FinanceCategory.SHOPPING -> Icons.Outlined.ShoppingBag
        FinanceCategory.BILLS -> Icons.AutoMirrored.Outlined.ReceiptLong
        FinanceCategory.ENTERTAINMENT -> Icons.Outlined.Movie
        FinanceCategory.HEALTH -> Icons.Outlined.FavoriteBorder
        FinanceCategory.TRAVEL -> Icons.Outlined.FlightTakeoff
        FinanceCategory.SAVINGS -> Icons.Outlined.Savings
        FinanceCategory.INVESTMENT -> Icons.AutoMirrored.Outlined.TrendingUp
        FinanceCategory.OTHER -> Icons.Outlined.Category
    }
}

private fun LocalDate.formatShortDate(): String {
    val monthLabel = month.name.lowercase().replaceFirstChar(Char::titlecase).take(3)
    return "${day} $monthLabel"
}

private fun LocalDate.formatLongDate(): String {
    val dayLabel = dayOfWeek.name.lowercase().replaceFirstChar(Char::titlecase).take(3)
    val monthLabel = month.name.lowercase().replaceFirstChar(Char::titlecase).take(3)
    return "$dayLabel, ${day} $monthLabel"
}
