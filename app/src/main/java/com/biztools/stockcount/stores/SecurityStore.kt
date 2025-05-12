package com.biztools.stockcount.stores

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import java.util.Calendar

val Context.securityStore by preferencesDataStore("security")
var previousInteractions = mutableIntStateOf(0)
var currentInteractions = mutableIntStateOf(0)

class SecurityStore(private val context: Context) {
    private val deviceKey = stringPreferencesKey("device")
    private val dateKey = stringPreferencesKey("days")
    val date = context.securityStore.data.map { it[dateKey] }
    val device = context.securityStore.data.map { it[deviceKey] }

    suspend fun secure(device: String) {
        context.securityStore.edit {
            it[deviceKey] = device
            it[dateKey] = Calendar.getInstance().timeInMillis.toString()
        }
    }
}