package com.biztools.stockcount.api

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

const val license: String = "U_Care_P26_User1"
const val project: String = "P26"

const val apiUrl: String = "http://192.168.3.8:8790"

class RestAPI {
    companion object {
        inline fun <reified T> create(
            token: String? = null,
            deviceId: String = "",
            headers: List<Header> = listOf(),
            basUrl: String = apiUrl
        ): T {
            val headerList = mutableListOf(
                Header("conicalhat-device-id", deviceId),
                Header("conicalhat-project-name", project),
                Header("conicalhat-client-identity", license),
                Header(
                    "conicalhat-security-checked",
                    if (deviceId.isNotEmpty() && deviceId.isNotBlank()) "b170d938faae17f455049f2c2d4b8c0f" else ""
                ),
            )
            if (headers.isNotEmpty()) headerList.addAll(headers)
            val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
            val client = CustomHttpClient.unsafeSslClient(token, headerList)
            val retrofit = Retrofit.Builder()
                .baseUrl("$basUrl/api/")
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
                                val obj = if (err != null) JSONObject(err.string()) else null
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