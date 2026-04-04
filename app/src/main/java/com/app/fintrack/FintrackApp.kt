package com.app.fintrack

import android.app.Application
import com.app.fintrack.di.appModule
import com.app.fintrack.di.dataModule
import com.app.fintrack.di.domainModule
import com.app.fintrack.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FintrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FintrackApp)
            modules(appModule, dataModule, domainModule, presentationModule)
        }
    }
}
