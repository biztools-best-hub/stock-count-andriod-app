package com.biztools.stockcount.stores

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.biztools.stockcount.models.ReservedBarcodeData
import com.google.gson.Gson
import kotlinx.coroutines.flow.map
import java.util.Calendar

val Context.barcodesStore by preferencesDataStore("barcodes")

class BarcodesStore(private val ctx: Context) {
    private val barcodesKey = stringPreferencesKey("barcodes")
    private fun toStoreData(code: ReservedBarcodeData): String {
        return "code=${code.code};;count=${code.count};;warehouse=${code.warehouse};;timestamp=${code.timestamp.timeInMillis}"
    }

    suspend fun modify(code: ReservedBarcodeData) {
        ctx.barcodesStore.edit {
            val old = it[barcodesKey]
            if (old.isNullOrEmpty() || old.isBlank()) {
                it[barcodesKey] = toStoreData(code)
                return@edit
            }
//            val oldList = old.split("|#|")
//                .map { d -> Gson().fromJson(d, ReservedBarcodeData::class.java) }
//                .toMutableList()
            val oldList = extractData(old)
            oldList.add(code)
            it[barcodesKey] = oldList.joinToString("\n") { d -> toStoreData(d) }
        }
    }

    suspend fun remove(code: String, warehouse: String) {
        ctx.barcodesStore.edit {
            val old = it[barcodesKey]
            if (old.isNullOrEmpty() || old.isBlank()) return@edit
//            val oldList = old.split("|#|")
//                .map { d -> Gson().fromJson(d, ReservedBarcodeData::class.java) }
//                .toMutableList()
            val oldList = extractData(old)
            oldList.removeIf { d ->
                (d.code == code && d.warehouse == warehouse) || d.timestamp.get(
                    Calendar.DATE
                ) >= Calendar.getInstance().get(Calendar.DATE)
            }
            it[barcodesKey] = oldList.joinToString("|#|") { d -> Gson().toJson(d) }
        }
    }

    private fun extractData(content: String): MutableList<ReservedBarcodeData> {
        return content.split("\n")
//                .map { d -> Gson().fromJson(d, ReservedBarcodeData::class.java) }
            .map { d ->
                val chunks = d.split(";;").filter { c -> c.isNotEmpty() && c.isNotBlank() }
                val codeChunk = chunks.find { c -> c.startsWith("code=") } ?: "code="
                val countChunk = chunks.find { c -> c.startsWith("count=") } ?: "count=1"
                val whChunk = chunks.find { c -> c.startsWith("warehouse=") } ?: "warehouse="
                val timeChunk = chunks.find { c -> c.startsWith("timestamp=") } ?: "timestamp="
                val time = timeChunk.removePrefix("timestamp=").toLongOrNull() ?: 0
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = time
                ReservedBarcodeData(
                    code = codeChunk.removePrefix("code="),
                    count = countChunk.removePrefix("count=").toIntOrNull() ?: 1,
                    warehouse = whChunk.removePrefix("warehouse="),
                    timestamp = calendar
                )
            }
            .toMutableList()
    }

    suspend fun removeIf(fn: (code: ReservedBarcodeData) -> Boolean) {
        ctx.barcodesStore.edit {
            val old = it[barcodesKey]
            if (old.isNullOrEmpty() || old.isBlank()) return@edit
            val oldList = extractData(old)
            oldList.removeIf { d ->
                fn(d) || d.timestamp >= Calendar.getInstance()
            }
            it[barcodesKey] = oldList.joinToString("|#|") { d -> Gson().toJson(d) }
        }
    }

    val codes = ctx.barcodesStore.data.map {
        if (it[barcodesKey].isNullOrEmpty() || it[barcodesKey].isNullOrBlank()) listOf()
        else {
//            val chunks = it[barcodesKey]!!.split("|#|")
//                .map { c -> Gson().fromJson(c, ReservedBarcodeData::class.java) }
            val chunks = extractData(it[barcodesKey]!!)
            chunks.distinctBy { d -> d.code + d.warehouse }.map { d ->
                ReservedBarcodeData(
                    code = d.code,
                    count = chunks.filter { c -> c.code == d.code && c.warehouse == d.warehouse }
                        .sumOf { c -> c.count },
                    timestamp = d.timestamp,
                    warehouse = d.warehouse
                )
            }.filter { d ->
                d.timestamp.get(Calendar.DATE) >= Calendar.getInstance().get(Calendar.DATE)
            }
        }
    }

    suspend fun clearCodes() {
        ctx.barcodesStore.edit { it.remove(barcodesKey) }
    }
}