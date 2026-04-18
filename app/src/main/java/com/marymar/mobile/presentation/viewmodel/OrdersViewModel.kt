package com.marymar.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.dto.OrderResponseDto
import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(OrdersUiState())
    val ui: StateFlow<OrdersUiState> = _ui.asStateFlow()

    fun load(sessionUserId: Long, sessionRole: Role) {
        _ui.value = _ui.value.copy(loading = true, error = null, role = sessionRole, sessionUserId = sessionUserId)

        viewModelScope.launch {
            val result = when (sessionRole) {
                Role.CLIENTE -> orderRepository.getOrdersByClient(sessionUserId)
                Role.MESERO -> orderRepository.getAllOrders()
                else -> orderRepository.getAllOrders()
            }

            when (result) {
                is ApiResult.Success -> {
                    val filtered = if (sessionRole == Role.MESERO) {
                        result.data.filter { it.meseroId == sessionUserId }
                    } else {
                        result.data
                    }
                    _ui.value = _ui.value.copy(loading = false, orders = filtered)
                }
                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(loading = false, error = result.message)
                }
            }
        }
    }

    fun setFilter(filter: OrderListFilter) {
        _ui.value = _ui.value.copy(filter = filter)
    }

    fun filteredOrders(): List<OrderResponseDto> {
        val filter = _ui.value.filter
        return _ui.value.orders.filter { order ->
            when (filter) {
                OrderListFilter.ACTIVOS -> !isFinished(order.estado)
                OrderListFilter.FINALIZADOS -> isFinished(order.estado)
                OrderListFilter.TODOS -> true
            }
        }.sortedByDescending { it.fecha }
    }

    private fun isFinished(status: String): Boolean {
        return status.equals("ENTREGADO", true) ||
                status.equals("CUENTA_PEDIDA", true) ||
                status.equals("PAGADO", true) ||
                status.equals("CANCELADO", true)
    }
}

enum class OrderListFilter { ACTIVOS, FINALIZADOS, TODOS }

data class OrdersUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val orders: List<OrderResponseDto> = emptyList(),
    val filter: OrderListFilter = OrderListFilter.ACTIVOS,
    val role: Role? = null,
    val sessionUserId: Long? = null
)
