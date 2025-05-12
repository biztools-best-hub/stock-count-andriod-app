package com.biztools.stockcount.api

import com.biztools.stockcount.BuildConfig
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

//const val license: String = "lkr_01PM_User4" //LK Chetra
//
//const val project: String = "LKR-PM"
//
//const val apiUrl: String = "http://172.21.8.59:8799"//LK official 2

class RestAPI {
    companion object {
        inline fun <reified T> create(
            token: String? = null,
            deviceId: String = "",
            headers: List<Header> = listOf(),
            basUrl: String = BuildConfig.apiUrl
        ): T {
            val headerList = mutableListOf(
                Header("conicalhat-device-id", deviceId),
                Header("conicalhat-project-name", BuildConfig.project),
                Header("conicalhat-client-identity", BuildConfig.license),
                Header(
                    "conicalhat-security-checked",
                    if (deviceId.isNotEmpty() && deviceId.isNotBlank()) "b170d938faae17f455049f2c2d4b8c0f" else ""
                ),
            )
            if (headers.isNotEmpty()) headerList.addAll(headers)
            val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
            val client = CustomHttpClient.unsafeSslClient(token, headerList)
            val retrofit = Retrofit.Builder().baseUrl("$basUrl/api/")
                .addConverterFactory(GsonConverterFactory.create(gson))
            retrofit.client(client)
            val retro = retrofit.build()
            return retro.create()
        }

        inline fun <T> execute(
            call: Call<T>,
            scope: CoroutineScope,
            crossinline onSuccess: suspend (data: T) -> Unit,
            crossinline onError: suspend (e: Throwable) -> Unit,
        ) {
            try {
                call.enqueue(object : Callback<T> {
                    override fun onResponse(call: Call<T>, response: Response<T>) {
                        when (response.code().toString()) {
                            "200" -> scope.launch { onSuccess(response.body()!!) }
                            "401" -> scope.launch { onError(Throwable("unauthorized")) }
                            "404" -> scope.launch { onError(Throwable("not found")) }
                            else -> scope.launch {
                                val err = response.errorBody()
                                var obj: JSONObject? = null
                                try {
                                    obj = if (err != null) JSONObject(err.string()) else null
                                } catch (_: Exception) {
                                }
                                onError(
                                    Throwable(
                                        if (obj == null) response.message() else obj.getString("Message"),
                                        Throwable(response.code().toString())
                                    )
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<T>, t: Throwable) {
                        scope.launch { onError(t) }
                    }
                })
            } catch (e: Exception) {
                scope.launch { onError(e) }
            }
        }
    }
}