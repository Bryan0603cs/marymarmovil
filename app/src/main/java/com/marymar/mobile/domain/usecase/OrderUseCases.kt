package com.marymar.mobile.domain.usecase

import com.marymar.mobile.domain.model.CartItem
import com.marymar.mobile.domain.repository.OrderRepository
import javax.inject.Inject

class CreateDeliveryOrderUseCase @Inject constructor(
    private val repo: OrderRepository
) {
    suspend operator fun invoke(clienteId: Long, items: List<CartItem>) =
        repo.createDeliveryOrder(clienteId, items)
}

class GetOrdersByClientUseCase @Inject constructor(
    private val repo: OrderRepository
) {
    suspend operator fun invoke(clienteId: Long) = repo.getOrdersByClient(clienteId)
}
