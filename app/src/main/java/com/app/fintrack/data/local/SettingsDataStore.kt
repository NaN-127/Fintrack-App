package com.app.fintrack.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "fintrack_settings")
