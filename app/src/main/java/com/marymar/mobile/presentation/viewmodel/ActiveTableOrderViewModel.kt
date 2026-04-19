package com.marymar.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.dto.OrderResponseDto
import com.marymar.mobile.domain.model.Product
import com.marymar.mobile.domain.model.UploadAttachment
import com.marymar.mobile.domain.repository.OrderRepository
import com.marymar.mobile.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ActiveTableOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ActiveTableOrderUiState())
    val ui: StateFlow<ActiveTableOrderUiState> = _ui.asStateFlow()

    fun load(mesaId: Long, meseroId: Long) {
        _ui.value = _ui.value.copy(
            loading = true,
            error = null,
            message = null,
            mesaId = mesaId,
            meseroId = meseroId
        )

        viewModelScope.launch {
            val productsResult = productRepository.getProducts()
            val orderResult = when (val existing = orderRepository.getOrderByTable(mesaId)) {
                is ApiResult.Success -> existing
                is ApiResult.Error -> orderRepository.openOrderForTable(mesaId, meseroId)
            }

            val products = when (productsResult) {
                is ApiResult.Success -> productsResult.data
                is ApiResult.Error -> emptyList()
            }

            when (orderResult) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        loading = false,
                        products = products,
                        order = orderResult.data,
                        error = if (productsResult is ApiResult.Error) productsResult.message else null
                    )
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        loading = false,
                        products = products,
                        error = orderResult.message
                    )
                }
            }
        }
    }

    fun refresh() {
        val mesaId = _ui.value.mesaId ?: return
        val meseroId = _ui.value.meseroId ?: return
        load(mesaId, meseroId)
    }

    fun addProduct(productId: Long, quantity: Int = 1) {
        val order = _ui.value.order ?: return
        val safeQuantity = quantity.coerceAtLeast(1)

        _ui.value = _ui.value.copy(
            actionLoading = true,
            error = null,
            message = null
        )

        viewModelScope.launch {
            when (val result = orderRepository.addProductToOrder(order.id, productId, safeQuantity)) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        actionLoading = false,
                        order = result.data,
                        message = if (safeQuantity == 1) {
                            "Producto agregado"
                        } else {
                            "$safeQuantity productos agregados"
                        }
                    )
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        actionLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun decreaseProduct(productId: Long) {
        val order = _ui.value.order ?: return
        _ui.value = _ui.value.copy(actionLoading = true, error = null, message = null)

        viewModelScope.launch {
            when (val result = orderRepository.decreaseProduct(order.id, productId)) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        actionLoading = false,
                        order = result.data,
                        message = "Cantidad actualizada"
                    )
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        actionLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun removeDetail(detailId: Long) {
        val order = _ui.value.order ?: return
        _ui.value = _ui.value.copy(actionLoading = true, error = null, message = null)

        viewModelScope.launch {
            when (val result = orderRepository.removeDetail(order.id, detailId)) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        actionLoading = false,
                        order = result.data,
                        message = "Producto eliminado del pedido"
                    )
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        actionLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun advanceStatus(nextStatus: String) {
        val order = _ui.value.order ?: return
        _ui.value = _ui.value.copy(actionLoading = true, error = null, message = null)

        viewModelScope.launch {
            when (val result = orderRepository.changeOrderStatus(order.id, nextStatus)) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        actionLoading = false,
                        order = result.data,
                        message = "Estado actualizado"
                    )
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        actionLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun registerPayment(
        method: String,
        attachment: UploadAttachment? = null,
        onSuccess: () -> Unit
    ) {
        val order = _ui.value.order ?: return
        _ui.value = _ui.value.copy(actionLoading = true, error = null, message = null)

        viewModelScope.launch {
            when (
                val result = orderRepository.registerPayment(
                    orderId = order.id,
                    method = method,
                    amount = order.total,
                    attachment = attachment
                )
            ) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        actionLoading = false,
                        message = buildPaymentMessage(method),
                        order = order.copy(estado = "PAGADO")
                    )
                    onSuccess()
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        actionLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun markComandaDownloaded(orderId: Long) {
        _ui.value = _ui.value.copy(
            downloadedCommandOrders = _ui.value.downloadedCommandOrders + orderId
        )
    }

    fun cancelTable(onSuccess: () -> Unit) {
        val mesaId = _ui.value.mesaId ?: return
        _ui.value = _ui.value.copy(actionLoading = true, error = null, message = null)

        viewModelScope.launch {
            when (val result = orderRepository.cancelTable(mesaId)) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        actionLoading = false,
                        order = null,
                        message = "Mesa T${result.data.numero} cancelada correctamente"
                    )
                    onSuccess()
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        actionLoading = false,
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

    private fun buildPaymentMessage(method: String): String {
        return when (method.uppercase()) {
            "EFECTIVO" -> "Pago en efectivo registrado correctamente"
            "TARJETA" -> "Pago con tarjeta registrado correctamente"
            "TRANSFERENCIA" -> "Pago por transferencia registrado correctamente"
            else -> "Pago registrado correctamente"
        }
    }
}

data class ActiveTableOrderUiState(
    val loading: Boolean = false,
    val actionLoading: Boolean = false,
    val mesaId: Long? = null,
    val meseroId: Long? = null,
    val order: OrderResponseDto? = null,
    val products: List<Product> = emptyList(),
    val downloadedCommandOrders: Set<Long> = emptySet(),
    val error: String? = null,
    val message: String? = null
)