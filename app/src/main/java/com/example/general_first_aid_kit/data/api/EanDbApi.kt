package com.example.general_first_aid_kit.data.api

import retrofit2.http.Path
import retrofit2.http.GET
import retrofit2.http.Header

interface EanDbApi {
    @GET("v2/product/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String,
        @Header("Authorization") token: String
    ): EanDbResponse
}

data class EanDbResponse(
    val product: EanProduct?
)

data class EanProduct(
    val titles: Map<String, String>?,
    val categories: List<EanCategory>?,
    val barcode: String?
)

data class EanCategory(
    val titles: Map<String, String>?
)