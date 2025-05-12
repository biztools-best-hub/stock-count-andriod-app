package com.biztools.stockcount.stores

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.biztools.stockcount.models.POConfiguration
import kotlinx.coroutines.flow.map

val Context.poConfigStore by preferencesDataStore("config")

class POConfigStore(private val ctx: Context) {
    private val warehouseKey = stringPreferencesKey("warehouse")
    private val s1key = stringPreferencesKey("segment1")
    private val s2key = stringPreferencesKey("segment2")
    private val s4key = stringPreferencesKey("segment4")
    private val s5key = stringPreferencesKey("segment5")
    private val s6key = stringPreferencesKey("segment6")
    private val s7key = stringPreferencesKey("segment7")
    private val s8key = stringPreferencesKey("segment8")
    private val s9key = stringPreferencesKey("segment9")
    private val s10key = stringPreferencesKey("segment10")
    val config = ctx.poConfigStore.data.map {
        val wh = it[warehouseKey] ?: ""
        val s1 = it[s1key] ?: ""
        val s2 = it[s2key] ?: ""
        val s4 = it[s4key] ?: ""
        val s5 = it[s5key] ?: ""
        val s6 = it[s6key] ?: ""
        val s7 = it[s7key] ?: ""
        val s8 = it[s8key] ?: ""
        val s9 = it[s9key] ?: ""
        val s10 = it[s10key] ?: ""
        POConfiguration(wh, s1, s2, s4, s5, s6, s7, s8, s9, s10)
    }

    suspend fun setConfig(input: POConfiguration) {
        ctx.poConfigStore.edit {
            it[warehouseKey] = input.warehouse
            it[s1key] = input.s1
            it[s2key] = input.s2
            it[s4key] = input.s4
            it[s5key] = input.s5
            it[s6key] = input.s6
            it[s7key] = input.s7
            it[s8key] = input.s8
            it[s9key] = input.s9
            it[s10key] = input.s10
        }
    }

    suspend fun clear() {
        ctx.poConfigStore.edit {
            it.remove(warehouseKey)
            it.remove(s1key)
            it.remove(s2key)
            it.remove(s4key)
            it.remove(s5key)
            it.remove(s6key)
            it.remove(s7key)
            it.remove(s8key)
            it.remove(s9key)
            it.remove(s10key)
        }
    }
}