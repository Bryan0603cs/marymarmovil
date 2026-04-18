package com.marymar.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.domain.model.CartItem
import com.marymar.mobile.domain.model.Product
import com.marymar.mobile.domain.usecase.CreateDeliveryOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CartViewModel @Inject constructor(
    private val createDeliveryOrder: CreateDeliveryOrderUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(CartUiState())
    val ui: StateFlow<CartUiState> = _ui.asStateFlow()

    fun add(product: Product) {
        val current = _ui.value.items.toMutableList()
        val idx = current.indexOfFirst { it.product.id == product.id }
        if (idx >= 0) {
            val item = current[idx]
            current[idx] = item.copy(quantity = item.quantity + 1)
        } else {
            current.add(CartItem(product, 1))
        }
        _ui.value = _ui.value.copy(items = current, error = null, message = null)
    }

    fun remove(productId: Long) {
        val current = _ui.value.items.toMutableList()
        current.removeAll { it.product.id == productId }
        _ui.value = _ui.value.copy(items = current)
    }

    fun increase(productId: Long) {
        val current = _ui.value.items.toMutableList()
        val idx = current.indexOfFirst { it.product.id == productId }
        if (idx >= 0) {
            val item = current[idx]
            current[idx] = item.copy(quantity = item.quantity + 1)
            _ui.value = _ui.value.copy(items = current)
        }
    }

    fun decrease(productId: Long) {
        val current = _ui.value.items.toMutableList()
        val idx = current.indexOfFirst { it.product.id == productId }
        if (idx >= 0) {
            val item = current[idx]
            if (item.quantity <= 1) current.removeAt(idx) else current[idx] = item.copy(quantity = item.quantity - 1)
            _ui.value = _ui.value.copy(items = current)
        }
    }

    fun clear() {
        _ui.value = _ui.value.copy(items = emptyList(), error = null, message = null)
    }

    fun total(): Double = _ui.value.items.sumOf { it.product.price * it.quantity }

    fun totalItems(): Int = _ui.value.items.sumOf { it.quantity }

    fun placeDeliveryOrder(clienteId: Long) {
        if (_ui.value.items.isEmpty()) {
            _ui.value = _ui.value.copy(error = "Tu carrito está vacío")
            return
        }

        _ui.value = _ui.value.copy(loading = true, error = null, orderCreatedId = null, message = null)
        viewModelScope.launch {
            when (val res = createDeliveryOrder(clienteId, _ui.value.items)) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        loading = false,
                        items = emptyList(),
                        orderCreatedId = res.data.id,
                        message = "Pedido creado correctamente"
                    )
                }
                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(loading = false, error = res.message)
                }
            }
        }
    }

    fun consumeOrderCreated() {
        _ui.value = _ui.value.copy(orderCreatedId = null)
    }

    fun clearMessage() {
        _ui.value = _ui.value.copy(message = null, error = null)
    }
}

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val orderCreatedId: Long? = null
)
