package com.app.fintrack.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.fintrack.presentation.ai.AiAssistantRoute
import com.app.fintrack.presentation.home.HomeRoute
import com.app.fintrack.presentation.insights.InsightsRoute
import com.app.fintrack.presentation.splash.SplashRoute
import com.app.fintrack.presentation.transactions.TransactionFormRoute
import com.app.fintrack.presentation.transactions.TransactionsRoute

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Transactions : Screen("transactions")
    data object TransactionForm : Screen("transaction-form/{transactionId}") {
        fun createRoute(transactionId: Long? = null): String = "transaction-form/${transactionId ?: -1}"
    }
    data object Insights : Screen("insights")
    data object Ai : Screen("ai")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun FintrackRoot() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem(Screen.Home, "Home", Icons.Outlined.Home),
        BottomNavItem(Screen.Transactions, "Transactions", Icons.AutoMirrored.Outlined.ReceiptLong),
        BottomNavItem(Screen.Ai, "FinTrack AI", Icons.Outlined.ChatBubbleOutline),
        BottomNavItem(Screen.Insights, "Insights", Icons.Outlined.AutoGraph),
    )
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = navBackStackEntry?.destination
    val isImeVisible = WindowInsets.isImeVisible
    val shouldShowBottomBar = currentDestination?.route in setOf(
        Screen.Home.route,
        Screen.Transactions.route,
        Screen.Ai.route,
        Screen.Insights.route,
    ) && !isImeVisible

    BackHandler(enabled = currentDestination?.route != Screen.Home.route) {
        val popped = navController.popBackStack()
        if (!popped) {
            navController.navigate(Screen.Home.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 34.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 10.dp,
                    ) {
                        NavigationBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                        ) {
                            items.forEach { item ->
                                val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (!selected) {
                                            navController.navigateTopLevel(item.screen.route)
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                        )
                                    },
                                    label = { Text(text = item.label) },
                                    alwaysShowLabel = true,
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .background(MaterialTheme.colorScheme.background),
        ) {
            NavHost(navController = navController, startDestination = Screen.Splash.route, modifier = Modifier.fillMaxSize()) {
                composable(Screen.Splash.route) {
                    SplashRoute(
                        onFinished = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(Screen.Home.route) {
                    HomeRoute(
                        onViewAllTransactions = { navController.navigateTopLevel(Screen.Transactions.route) },
                        onAddTransaction = { navController.navigate(Screen.TransactionForm.createRoute()) },
                    )
                }
                composable(Screen.Transactions.route) {
                    TransactionsRoute(
                        onAddTransaction = { navController.navigate(Screen.TransactionForm.createRoute()) },
                        onEditTransaction = { navController.navigate(Screen.TransactionForm.createRoute(it)) },
                    )
                }
                composable(Screen.TransactionForm.route) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("transactionId")?.toLongOrNull()?.takeIf { it > 0L }
                    TransactionFormRoute(transactionId = id, onSaved = { navController.popBackStack() }, onBack = { navController.popBackStack() })
                }
                composable(Screen.Insights.route) { InsightsRoute() }
                composable(Screen.Ai.route) { AiAssistantRoute() }
            }
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
