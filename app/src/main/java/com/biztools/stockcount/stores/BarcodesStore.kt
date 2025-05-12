package com.biztools.stockcount.stores

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
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
            val oldList = extractData(old)
            oldList.add(code)
            it[barcodesKey] = oldList.joinToString("\n") { d -> toStoreData(d) }
        }
    }

    suspend fun reset(codes: List<ReservedBarcodeData>) {
        ctx.barcodesStore.edit {
            it[barcodesKey] = codes.joinToString("\n") { d -> toStoreData(d) }
        }
    }

    private fun extractData(content: String): MutableList<ReservedBarcodeData> {
        return content.split("\n")
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
            var oldList = extractData(old)
            oldList = oldList.filter { c -> fn(c) }.toMutableList()
            it[barcodesKey] = oldList.joinToString("|#|") { d -> Gson().toJson(d) }
        }
    }

    val codes = ctx.barcodesStore.data.map {
        if (it[barcodesKey].isNullOrEmpty() || it[barcodesKey].isNullOrBlank()) listOf()
        else {
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