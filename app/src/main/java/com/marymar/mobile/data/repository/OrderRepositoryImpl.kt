package com.marymar.mobile.data.repository

import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.api.OrderApi
import com.marymar.mobile.data.remote.dto.OrderCreateDto
import com.marymar.mobile.data.remote.dto.OrderDetailCreateDto
import com.marymar.mobile.data.remote.dto.OrderResponseDto
import com.marymar.mobile.domain.model.CartItem
import com.marymar.mobile.domain.repository.OrderRepository
import retrofit2.HttpException
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val api: OrderApi
) : OrderRepository {

    override suspend fun createOrder(
        clienteId: Long,
        meseroId: Long?,
        items: List<CartItem>
    ): ApiResult<OrderResponseDto> {
        return try {
            val body = OrderCreateDto(
                clienteId = clienteId,
                meseroId = meseroId,
                detalles = items.map { OrderDetailCreateDto(productoId = it.product.id, cantidad = it.quantity) }
            )
            ApiResult.Success(api.createOrder(body))
        } catch (e: HttpException) {
            ApiResult.Error("Error HTTP ${e.code()}", e.code(), e)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error inesperado", null, e)
        }
    }

    override suspend fun getOrdersByClient(clienteId: Long): ApiResult<List<OrderResponseDto>> {
        return try {
            ApiResult.Success(api.getOrdersByClient(clienteId))
        } catch (e: HttpException) {
            ApiResult.Error("Error HTTP ${e.code()}", e.code(), e)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error inesperado", null, e)
        }
    }
}
