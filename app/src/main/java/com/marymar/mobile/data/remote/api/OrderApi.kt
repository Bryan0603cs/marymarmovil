package com.marymar.mobile.data.remote.api

import com.marymar.mobile.data.remote.dto.OrderCreateDto
import com.marymar.mobile.data.remote.dto.OrderResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApi {

    @POST("api/pedidos")
    suspend fun createOrder(@Body body: OrderCreateDto): OrderResponseDto

    @GET("api/pedidos/cliente/{clienteId}")
    suspend fun getOrdersByClient(@Path("clienteId") clienteId: Long): List<OrderResponseDto>

    @GET("api/pedidos/{id}")
    suspend fun getOrder(@Path("id") id: Long): OrderResponseDto
}
