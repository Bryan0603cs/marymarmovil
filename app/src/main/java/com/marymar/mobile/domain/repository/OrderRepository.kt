package com.marymar.mobile.domain.repository

import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.domain.model.CartItem
import com.marymar.mobile.data.remote.dto.OrderResponseDto

interface OrderRepository {
    suspend fun createOrder(clienteId: Long, meseroId: Long?, items: List<CartItem>): ApiResult<OrderResponseDto>
    suspend fun getOrdersByClient(clienteId: Long): ApiResult<List<OrderResponseDto>>
}
