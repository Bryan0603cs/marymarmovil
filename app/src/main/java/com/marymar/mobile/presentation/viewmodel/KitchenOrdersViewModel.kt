package com.marymar.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.dto.OrderResponseDto
import com.marymar.mobile.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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

    fun load() {
        _ui.value = _ui.value.copy(
            loading = true,
            error = null,
            message = null
        )

        viewModelScope.launch {
            when (val result = orderRepository.getAllOrders()) {
                is ApiResult.Success -> {
                    val orders = result.data
                        .filter { it.tipo.equals("MESA", true) }
                        .sortedBy { it.id }

                    val currentStarted = _ui.value.preparationStartedAt
                    val updatedStarted = currentStarted.toMutableMap()

                    orders
                        .filter { it.estado.equals("EN_PREPARACION", true) }
                        .forEach { order ->
                            if (!updatedStarted.containsKey(order.id)) {
                                updatedStarted[order.id] =
                                    parseOrderDateToMillis(order.fecha) ?: System.currentTimeMillis()
                            }
                        }

                    _ui.value = _ui.value.copy(
                        loading = false,
                        allOrders = orders,
                        preparationStartedAt = updatedStarted
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

    fun startPreparation(order: OrderResponseDto) {
        _ui.value = _ui.value.copy(
            actionLoadingId = order.id,
            error = null,
            message = null
        )

        viewModelScope.launch {
            when (val result = orderRepository.changeOrderStatus(order.id, "EN_PREPARACION")) {
                is ApiResult.Success -> {
                    val now = System.currentTimeMillis()
                    _ui.value = _ui.value.copy(
                        actionLoadingId = null,
                        message = "Pedido #${order.id} pasó a preparación",
                        preparationStartedAt = _ui.value.preparationStartedAt + (order.id to now)
                    )
                    load()
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        actionLoadingId = null,
                        error = result.message
                    )
                }
            }
        }
    }

    fun markReady(order: OrderResponseDto) {
        _ui.value = _ui.value.copy(
            actionLoadingId = order.id,
            error = null,
            message = null
        )

        viewModelScope.launch {
            when (val result = orderRepository.changeOrderStatus(order.id, "LISTO")) {
                is ApiResult.Success -> {
                    val started = _ui.value.preparationStartedAt[order.id]
                    val finishedMap = if (started != null) {
                        _ui.value.preparationFinishedElapsed + (
                                order.id to (System.currentTimeMillis() - started).coerceAtLeast(0L)
                                )
                    } else {
                        _ui.value.preparationFinishedElapsed
                    }

                    _ui.value = _ui.value.copy(
                        actionLoadingId = null,
                        message = "Pedido #${order.id} marcado como listo",
                        preparationFinishedElapsed = finishedMap
                    )
                    load()
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        actionLoadingId = null,
                        error = result.message
                    )
                }
            }
        }
    }

    fun clearBanner() {
        _ui.value = _ui.value.copy(
            error = null,
            message = null
        )
    }

    private fun parseOrderDateToMillis(rawDate: String?): Long? {
        if (rawDate.isNullOrBlank()) return null

        return try {
            LocalDateTime.parse(rawDate)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (_: DateTimeParseException) {
            try {
                OffsetDateTime.parse(rawDate)
                    .toInstant()
                    .toEpochMilli()
            } catch (_: DateTimeParseException) {
                try {
                    Instant.parse(rawDate).toEpochMilli()
                } catch (_: DateTimeParseException) {
                    try {
                        LocalDateTime.parse(
                            rawDate,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    } catch (_: DateTimeParseException) {
                        null
                    }
                }
            }
        }
    }
}

data class KitchenOrdersUiState(
    val loading: Boolean = false,
    val actionLoadingId: Long? = null,
    val allOrders: List<OrderResponseDto> = emptyList(),
    val preparationStartedAt: Map<Long, Long> = emptyMap(),
    val preparationFinishedElapsed: Map<Long, Long> = emptyMap(),
    val error: String? = null,
    val message: String? = null
) {
    val newOrders: List<OrderResponseDto>
        get() = allOrders.filter {
            it.estado.equals("CREADO", true) || it.estado.equals("CONFIRMADO", true)
        }

    val preparingOrders: List<OrderResponseDto>
        get() = allOrders.filter { it.estado.equals("EN_PREPARACION", true) }
}