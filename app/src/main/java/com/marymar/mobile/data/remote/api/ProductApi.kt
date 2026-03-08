package com.marymar.mobile.data.remote.api

import com.marymar.mobile.data.remote.dto.ProductResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductApi {

    @GET("api/productos")
    suspend fun getProducts(): List<ProductResponseDto>

    @GET("api/productos/{id}")
    suspend fun getProductById(@Path("id") id: Long): ProductResponseDto
}
