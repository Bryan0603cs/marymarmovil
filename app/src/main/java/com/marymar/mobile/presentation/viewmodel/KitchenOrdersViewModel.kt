package com.marymar.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.dto.OrderResponseDto
import com.marymar.mobile.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class KitchenOrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(KitchenOrdersUiState())
    val ui: StateFlow<KitchenOrdersUiState> = _ui.asStateFlow()

    private val preparationStartedAt = mutableMapOf<Long, Long>()

    fun load() {
        _ui.value = _ui.value.copy(loading = true, error = null, message = null)

        viewModelScope.launch {
            when (val result = orderRepository.getAllOrders()) {
                is ApiResult.Success -> {
                    val kitchenOrders = result.data
                        .filter { it.estado.uppercase() != "CANCELADO" }
                        .filter { it.tipo.equals("MESA", true) || it.mesaId != null }
                        .sortedByDescending { it.fecha }

                    val queue = kitchenOrders.filter {
                        it.estado.equals("CREADO", true) || it.estado.equals("CONFIRMADO", true)
                    }

                    val preparing = kitchenOrders.filter {
                        it.estado.equals("EN_PREPARACION", true)
                    }

                    val ready = kitchenOrders.filter {
                        it.estado.equals("LISTO", true)
                    }

                    val preparingIds = preparing.map { it.id }.toSet()

                    preparing.forEach { order ->
                        if (!preparationStartedAt.containsKey(order.id)) {
                            preparationStartedAt[order.id] = System.currentTimeMillis()
                        }
                    }

                    val idsToRemove = preparationStartedAt.keys.filter { it !in preparingIds }
                    idsToRemove.forEach { preparationStartedAt.remove(it) }

                    _ui.value = _ui.value.copy(
                        loading = false,
                        queue = queue,
                        preparing = preparing,
                        ready = ready
                    )
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        loading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun startPreparation(orderId: Long) {
        _ui.value = _ui.value.copy(actionLoadingOrderId = orderId, error = null, message = null)

        viewModelScope.launch {
            when (val result = orderRepository.changeOrderStatus(orderId, "EN_PREPARACION")) {
                is ApiResult.Success -> {
                    preparationStartedAt[orderId] = System.currentTimeMillis()
                    _ui.value = _ui.value.copy(
                        actionLoadingOrderId = null,
                        message = "Pedido enviado a preparación"
                    )
                    load()
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        actionLoadingOrderId = null,
                        error = result.message
                    )
                }
            }
        }
    }

    fun markReady(orderId: Long) {
        _ui.value = _ui.value.copy(actionLoadingOrderId = orderId, error = null, message = null)

        viewModelScope.launch {
            when (val result = orderRepository.changeOrderStatus(orderId, "LISTO")) {
                is ApiResult.Success -> {
                    preparationStartedAt.remove(orderId)
                    _ui.value = _ui.value.copy(
                        actionLoadingOrderId = null,
                        message = "Pedido marcado como listo"
                    )
                    load()
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        actionLoadingOrderId = null,
                        error = result.message
                    )
                }
            }
        }
    }

    fun getPreparationStartedAt(orderId: Long): Long? = preparationStartedAt[orderId]

    fun clearBanner() {
        _ui.value = _ui.value.copy(error = null, message = null)
    }
}

data class KitchenOrdersUiState(
    val loading: Boolean = false,
    val actionLoadingOrderId: Long? = null,
    val queue: List<OrderResponseDto> = emptyList(),
    val preparing: List<OrderResponseDto> = emptyList(),
    val ready: List<OrderResponseDto> = emptyList(),
    val error: String? = null,
    val message: String? = null
)