package com.app.fintrack.di

import androidx.room.Room
import com.app.fintrack.data.local.FintrackDatabase
import com.app.fintrack.data.local.settingsDataStore
import com.app.fintrack.data.remote.FinanceAiRemoteDataSource
import com.app.fintrack.data.remote.KoogGeminiFinanceAiRemoteDataSource
import com.app.fintrack.data.repository.AiRepositoryImpl
import com.app.fintrack.data.repository.FinanceRepositoryImpl
import com.app.fintrack.data.repository.SavingsGoalRepositoryImpl
import com.app.fintrack.data.repository.UserPreferencesRepositoryImpl
import com.app.fintrack.domain.repository.AiRepository
import com.app.fintrack.domain.repository.FinanceRepository
import com.app.fintrack.domain.repository.SavingsGoalRepository
import com.app.fintrack.domain.repository.UserPreferencesRepository
import com.app.fintrack.domain.usecase.AskFinanceAssistantUseCase
import com.app.fintrack.domain.usecase.AddSavingsGoalUseCase
import com.app.fintrack.domain.usecase.DeleteSavingsGoalUseCase
import com.app.fintrack.domain.usecase.DeleteTransactionUseCase
import com.app.fintrack.domain.usecase.GetTransactionByIdUseCase
import com.app.fintrack.domain.usecase.ObserveBudgetLimitUseCase
import com.app.fintrack.domain.usecase.ObserveDashboardUseCase
import com.app.fintrack.domain.usecase.ObserveInsightsUseCase
import com.app.fintrack.domain.usecase.ObserveRecentTransactionsUseCase
import com.app.fintrack.domain.usecase.ObserveSavingsGoalsUseCase
import com.app.fintrack.domain.usecase.ObserveTransactionsUseCase
import com.app.fintrack.domain.usecase.ObserveThemeModeUseCase
import com.app.fintrack.domain.usecase.SaveTransactionUseCase
import com.app.fintrack.domain.usecase.SetBudgetLimitUseCase
import com.app.fintrack.domain.usecase.SetThemeModeUseCase
import com.app.fintrack.domain.usecase.UpdateSavingsGoalUseCase
import com.app.fintrack.presentation.ai.AiChatSessionStore
import com.app.fintrack.presentation.ai.AiAssistantViewModel
import com.app.fintrack.presentation.goals.GoalsViewModel
import com.app.fintrack.presentation.home.HomeViewModel
import com.app.fintrack.presentation.insights.InsightsViewModel
import com.app.fintrack.presentation.settings.ThemeSettingsViewModel
import com.app.fintrack.presentation.transactions.TransactionFormViewModel
import com.app.fintrack.presentation.transactions.TransactionsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }
    single {
        Room.databaseBuilder(get(), FintrackDatabase::class.java, "fintrack.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { get<FintrackDatabase>().transactionDao() }
    single { get<FintrackDatabase>().savingsGoalDao() }
    single { get<android.content.Context>().settingsDataStore }
}

val dataModule = module {
    single { AiChatSessionStore() }
    single<FinanceAiRemoteDataSource> { KoogGeminiFinanceAiRemoteDataSource(get()) }
    single<FinanceRepository> { FinanceRepositoryImpl(get()) }
    single<AiRepository> { AiRepositoryImpl(get(), get()) }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(get()) }
    single<SavingsGoalRepository> { SavingsGoalRepositoryImpl(get()) }
}

val domainModule = module {
    factory { ObserveDashboardUseCase(get()) }
    factory { ObserveInsightsUseCase(get()) }
    factory { ObserveRecentTransactionsUseCase(get()) }
    factory { ObserveTransactionsUseCase(get()) }
    factory { GetTransactionByIdUseCase(get()) }
    factory { SaveTransactionUseCase(get()) }
    factory { DeleteTransactionUseCase(get()) }
    factory { AskFinanceAssistantUseCase(get()) }
    factory { ObserveThemeModeUseCase(get()) }
    factory { SetThemeModeUseCase(get()) }
    factory { ObserveBudgetLimitUseCase(get()) }
    factory { SetBudgetLimitUseCase(get()) }
    factory { ObserveSavingsGoalsUseCase(get()) }
    factory { AddSavingsGoalUseCase(get()) }
    factory { UpdateSavingsGoalUseCase(get()) }
    factory { DeleteSavingsGoalUseCase(get()) }
}

val presentationModule = module {
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { TransactionsViewModel(get(), get()) }
    viewModel { parameters -> TransactionFormViewModel(parameters.getOrNull(), get(), get()) }
    viewModel { InsightsViewModel(get()) }
    viewModel { GoalsViewModel(get(), get()) }
    viewModel { AiAssistantViewModel(get(), get(), get(), get()) }
    viewModel { ThemeSettingsViewModel(get(), get()) }
}
