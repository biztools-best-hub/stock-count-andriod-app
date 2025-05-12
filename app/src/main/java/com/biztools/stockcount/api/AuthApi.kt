package com.biztools.stockcount.api

import com.biztools.stockcount.models.AppValidationResult
import com.biztools.stockcount.models.LicenseResult
import com.biztools.stockcount.models.LoginInput
import com.biztools.stockcount.models.LoginResult
import com.biztools.stockcount.models.ValidationAndWarehouses
import com.biztools.stockcount.models.ValidationAndWhoAmI
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/login")
    fun login(@Body input: LoginInput): Call<LoginResult>

    @GET("auth/validate-app")
    fun validateApp(): Call<AppValidationResult>

    @GET("auth/create-license")
    fun createLicense(@Query("license") license: String): Call<LicenseResult>

    @GET("auth/validate-and-get-warehouses")
    fun validateAppAndGetWarehouses(): Call<ValidationAndWarehouses>

    @GET("auth/validate-and-who-am-i")
    fun validateAppAndWhoAmI(): Call<ValidationAndWhoAmI>
}