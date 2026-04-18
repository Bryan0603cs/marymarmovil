package com.marymar.mobile.data.remote.api

import com.marymar.mobile.data.remote.dto.OrderCreateDto
import com.marymar.mobile.data.remote.dto.OrderResponseDto
import com.marymar.mobile.data.remote.dto.TableResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApi {

    @POST("api/pedidos")
    suspend fun createOrder(@Body body: OrderCreateDto): OrderResponseDto

    @GET("api/pedidos/cliente/{clienteId}")
    suspend fun getOrdersByClient(@Path("clienteId") clienteId: Long): List<OrderResponseDto>

    @GET("api/pedidos")
    suspend fun getAllOrders(): List<OrderResponseDto>

    @GET("api/pedidos/{id}")
    suspend fun getOrder(@Path("id") id: Long): OrderResponseDto

    @PUT("api/pedidos/{id}/estado")
    suspend fun updateOrderStatus(
        @Path("id") id: Long,
        @Query("estado") estado: String
    ): OrderResponseDto

    @GET("api/pedidos/mesa/{mesaId}")
    suspend fun getOrderByTable(@Path("mesaId") mesaId: Long): OrderResponseDto

    @POST("api/pedidos/mesa/{mesaId}/abrir")
    suspend fun openOrderForTable(
        @Path("mesaId") mesaId: Long,
        @Query("meseroId") meseroId: Long
    ): OrderResponseDto

    @POST("api/pedidos/{pedidoId}/agregar-producto")
    suspend fun addProductToOrder(
        @Path("pedidoId") pedidoId: Long,
        @Query("productoId") productoId: Long,
        @Query("cantidad") cantidad: Int
    ): OrderResponseDto

    @PUT("api/pedidos/{pedidoId}/disminuir-producto")
    suspend fun decreaseProduct(
        @Path("pedidoId") pedidoId: Long,
        @Query("productoId") productoId: Long
    ): OrderResponseDto

    @DELETE("api/pedidos/{pedidoId}/detalle/{detalleId}")
    suspend fun removeDetail(
        @Path("pedidoId") pedidoId: Long,
        @Path("detalleId") detalleId: Long
    ): OrderResponseDto

    @GET("api/mesas")
    suspend fun getTables(): List<TableResponseDto>

    @POST("api/mesas/{id}/abrir")
    suspend fun openTable(
        @Path("id") tableId: Long,
        @Query("meseroId") meseroId: Long
    ): TableResponseDto

    @POST("api/mesas/{id}/cancelar")
    suspend fun cancelTable(@Path("id") tableId: Long): TableResponseDto
}
