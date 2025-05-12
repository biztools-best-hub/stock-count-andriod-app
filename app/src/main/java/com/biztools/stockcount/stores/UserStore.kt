package com.biztools.stockcount.stores

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.biztools.stockcount.models.User
import kotlinx.coroutines.flow.map
import java.util.Calendar

val Context.userStore by preferencesDataStore("user")

class UserStore(private val ctx: Context) {
    private val usernameKey = stringPreferencesKey("username")
    private val userOidKey = stringPreferencesKey("oid")
    private val tokenKey = stringPreferencesKey("token")
    private val passwordKey = stringPreferencesKey("password")
    private val lastLoginDateKey = stringPreferencesKey("last-login")
    val user = ctx.userStore.data.map {
        val oid = it[userOidKey] ?: ""
        val token = it[tokenKey] ?: ""
        val lastLogin = it[lastLoginDateKey] ?: ""
        val username = it[usernameKey] ?: ""
        val password = it[passwordKey] ?: ""
        val chunks = if (lastLogin.isEmpty()) listOf() else lastLogin.split(",")
        var d = 0
        var m = 0
        var y = 0
        var h = 0
        var mn = 0
        var s = 0
        if (chunks.isNotEmpty()) d = chunks[0].toInt()
        if (chunks.size > 1) m = chunks[1].toInt()
        if (chunks.size > 2) y = chunks[2].toInt()
        if (chunks.size > 3) h = chunks[3].toInt()
        if (chunks.size > 4) mn = chunks[4].toInt()
        if (chunks.size > 5) s = chunks[5].toInt()
        val calendar = Calendar.getInstance()
        calendar.set(y, m, d, h, mn, s)
        if (username.isEmpty() || username.isBlank()) null
        else User(username, oid, token, password, calendar)
    }

    suspend fun setUser(username: String, oid: String, token: String, password: String) {
        ctx.userStore.edit {
            it[usernameKey] = username
            it[userOidKey] = oid
            it[tokenKey] = token
            it[passwordKey] = password
            val calendar = Calendar.getInstance()
            val d = calendar.get(Calendar.DAY_OF_MONTH)
            val m = calendar.get(Calendar.MONTH)
            val y = calendar.get(Calendar.YEAR)
            val h = calendar.get(Calendar.HOUR)
            val mn = calendar.get(Calendar.MINUTE)
            val s = calendar.get(Calendar.SECOND)
            it[lastLoginDateKey] = listOf(d, m, y, h, mn, s).joinToString(separator = ",")
        }
    }

    suspend fun removeUser() {
        ctx.userStore.edit {
            it.remove(usernameKey)
            it.remove(userOidKey)
            it.remove(tokenKey)
            it.remove(lastLoginDateKey)
            it.remove(passwordKey)
        }
    }
}