package com.marymar.mobile.domain.usecase

import com.marymar.mobile.domain.model.CartItem
import com.marymar.mobile.domain.repository.OrderRepository
import javax.inject.Inject

class CreateOrderUseCase @Inject constructor(
    private val repo: OrderRepository
) {
    suspend operator fun invoke(clienteId: Long, meseroId: Long?, items: List<CartItem>) =
        repo.createOrder(clienteId, meseroId, items)
}

class GetOrdersByClientUseCase @Inject constructor(
    private val repo: OrderRepository
) {
    suspend operator fun invoke(clienteId: Long) = repo.getOrdersByClient(clienteId)
}
