package com.biztools.stockcount.api

import com.biztools.stockcount.models.AddItemsInput
import com.biztools.stockcount.models.GetWarehousesResult
import com.biztools.stockcount.models.ItemInfo
import com.biztools.stockcount.models.ItemsResult
import com.biztools.stockcount.models.PrintBarcodesInput
import com.biztools.stockcount.models.PrintBarcodesResult
import com.biztools.stockcount.models.RateCard
import com.biztools.stockcount.models.SaveStockResult
import com.biztools.stockcount.models.StockCountBatchInput
import com.biztools.stockcount.models.StockOrderInput
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface StockApi {
    @POST("stock/save-stock-file")
    fun saveStockFile(@Body input: StockCountBatchInput): Call<SaveStockResult>

    @GET("stock/check-item-info")
    fun checkItemInfo(
        @Query("warehouse") warehouse: String,
        @Query("number") number: String
    ): Call<ItemInfo>

    @GET("stock/check-item-info-non-strict")
    fun checkItemInfoNonStrict(@Query("number") number: String): Call<ItemInfo>

    @GET("stock/items")
    fun getItems(@Query("page") page: Int = 1, @Query("count") count: Int = 0): Call<ItemsResult>

    @GET("stock/get-rate-cards")
    fun getRateCards(): Call<List<RateCard>>

    @POST("stock/print-batch-barcodes")
    fun printBatchBarcodes(@Body input: PrintBarcodesInput): Call<PrintBarcodesResult>

    @GET("stock/warehouses")
    fun getWarehouses(): Call<GetWarehousesResult>

    @POST("stock/create-po")
    fun createPO(@Body input: StockOrderInput): Call<PrintBarcodesResult>

    @GET("stock/add-item")
    fun addItem(@Query("number") number: String): Call<PrintBarcodesResult>

    @POST("stock/add-items")
    fun addItems(@Body input: AddItemsInput): Call<PrintBarcodesResult>
}