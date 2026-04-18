package com.marymar.mobile.domain.repository

import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.dto.OrderResponseDto
import com.marymar.mobile.data.remote.dto.TableResponseDto
import com.marymar.mobile.domain.model.CartItem

interface OrderRepository {
    suspend fun createDeliveryOrder(clienteId: Long, items: List<CartItem>): ApiResult<OrderResponseDto>
    suspend fun getOrdersByClient(clienteId: Long): ApiResult<List<OrderResponseDto>>
    suspend fun getAllOrders(): ApiResult<List<OrderResponseDto>>
    suspend fun getOrder(id: Long): ApiResult<OrderResponseDto>
    suspend fun changeOrderStatus(orderId: Long, status: String): ApiResult<OrderResponseDto>

    suspend fun getTables(): ApiResult<List<TableResponseDto>>
    suspend fun openTable(tableId: Long, meseroId: Long): ApiResult<TableResponseDto>
    suspend fun cancelTable(tableId: Long): ApiResult<TableResponseDto>

    suspend fun getOrderByTable(tableId: Long): ApiResult<OrderResponseDto>
    suspend fun openOrderForTable(tableId: Long, meseroId: Long): ApiResult<OrderResponseDto>
    suspend fun addProductToOrder(orderId: Long, productId: Long, quantity: Int): ApiResult<OrderResponseDto>
    suspend fun decreaseProduct(orderId: Long, productId: Long): ApiResult<OrderResponseDto>
    suspend fun removeDetail(orderId: Long, detailId: Long): ApiResult<OrderResponseDto>
}
