package com.marymar.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.dto.OrderResponseDto
import com.marymar.mobile.data.remote.dto.TableResponseDto
import com.marymar.mobile.domain.model.UploadAttachment
import com.marymar.mobile.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TablesViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(TablesUiState())
    val ui: StateFlow<TablesUiState> = _ui.asStateFlow()

    fun load() {
        _ui.value = _ui.value.copy(loading = true, error = null, message = null)

        viewModelScope.launch {
            val tablesDeferred = async { orderRepository.getTables() }
            val ordersDeferred = async { orderRepository.getAllOrders() }

            val tablesResult = tablesDeferred.await()
            val ordersResult = ordersDeferred.await()

            val tables = when (tablesResult) {
                is ApiResult.Success -> tablesResult.data
                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(loading = false, error = tablesResult.message)
                    return@launch
                }
            }

            val orders = when (ordersResult) {
                is ApiResult.Success -> ordersResult.data
                is ApiResult.Error -> emptyList()
            }

            _ui.value = _ui.value.copy(
                loading = false,
                tables = tables,
                orders = orders,
                cards = buildCards(tables, orders),
                error = if (ordersResult is ApiResult.Error) ordersResult.message else null
            )
        }
    }

    fun openTable(tableId: Long, meseroId: Long, onSuccess: () -> Unit) {
        _ui.value = _ui.value.copy(actionLoadingTableId = tableId, error = null, message = null)

        viewModelScope.launch {
            when (val result = orderRepository.openTable(tableId, meseroId)) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        actionLoadingTableId = null,
                        message = "Mesa T${result.data.numero} abierta correctamente"
                    )
                    load()
                    onSuccess()
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(actionLoadingTableId = null, error = result.message)
                }
            }
        }
    }

    fun registerPayment(
        orderId: Long,
        tableId: Long,
        amount: Double,
        method: String,
        attachment: UploadAttachment? = null
    ) {
        _ui.value = _ui.value.copy(actionLoadingTableId = tableId, error = null, message = null)

        viewModelScope.launch {
            when (val result = orderRepository.registerPayment(orderId, method, amount, attachment)) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        actionLoadingTableId = null,
                        message = buildPaymentMessage(method)
                    )
                    load()
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(actionLoadingTableId = null, error = result.message)
                }
            }
        }
    }

    fun clearBanner() {
        _ui.value = _ui.value.copy(error = null, message = null)
    }

    private fun buildCards(
        tables: List<TableResponseDto>,
        orders: List<OrderResponseDto>
    ): List<WaiterTableCard> {
        val activeOrdersByMesa = orders
            .filter { it.tipo.equals("MESA", true) }
            .filter { it.mesaId != null }
            .groupBy { it.mesaId!! }
            .mapValues { (_, tableOrders) ->
                tableOrders
                    .filterNot { isFinished(it.estado) }
                    .maxByOrNull { it.fecha }
            }

        return tables
            .sortedBy { it.numero }
            .map { table ->
                WaiterTableCard(
                    table = table,
                    order = activeOrdersByMesa[table.id]
                )
            }
    }

    private fun isFinished(status: String): Boolean {
        return status.equals("PAGADO", true) || status.equals("CANCELADO", true)
    }

    private fun buildPaymentMessage(method: String): String {
        return when (method.uppercase()) {
            "EFECTIVO" -> "Pago en efectivo registrado correctamente"
            "TARJETA" -> "Pago con tarjeta registrado correctamente"
            "TRANSFERENCIA" -> "Pago por transferencia registrado correctamente"
            else -> "Pago registrado correctamente"
        }
    }
}

data class WaiterTableCard(
    val table: TableResponseDto,
    val order: OrderResponseDto? = null
)

data class TablesUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val actionLoadingTableId: Long? = null,
    val tables: List<TableResponseDto> = emptyList(),
    val orders: List<OrderResponseDto> = emptyList(),
    val cards: List<WaiterTableCard> = emptyList()
)