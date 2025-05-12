package com.biztools.stockcount.stores

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.settingStore by preferencesDataStore("setting")

class SettingStore(private val context: Context) {
    private val darkKey = stringPreferencesKey("dark")
    private val autoScanModeKey = stringPreferencesKey("auto-scan")
    val isDark = context.settingStore.data.map { it[darkKey] == "1" }
    val isAutoScanMode = context.settingStore.data.map { it[autoScanModeKey] == "1" }

    suspend fun toggleScanMode() {
        context.settingStore.edit {
            it[autoScanModeKey] = if (it[autoScanModeKey] == "1") "0" else "1"
        }
    }
}