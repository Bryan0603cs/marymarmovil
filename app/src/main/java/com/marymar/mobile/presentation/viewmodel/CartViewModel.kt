package com.marymar.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.domain.model.CartItem
import com.marymar.mobile.domain.model.Product
import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.domain.usecase.CreateOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val createOrder: CreateOrderUseCase
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
        _ui.value = _ui.value.copy(items = current)
    }

    fun remove(productId: Long) {
        val current = _ui.value.items.toMutableList()
        current.removeAll { it.product.id == productId }
        _ui.value = _ui.value.copy(items = current)
    }

    fun setQty(productId: Long, qty: Int) {
        val current = _ui.value.items.toMutableList()
        val idx = current.indexOfFirst { it.product.id == productId }
        if (idx >= 0) {
            if (qty <= 0) current.removeAt(idx)
            else current[idx] = current[idx].copy(quantity = qty)
            _ui.value = _ui.value.copy(items = current)
        }
    }

    fun clear() {
        _ui.value = _ui.value.copy(items = emptyList())
    }

    fun total(): Double = _ui.value.items.sumOf { it.product.price * it.quantity }

    /**
     * CLIENTE: clienteId = sessionUserId, meseroId = null
     * MESERO:  clienteId = clienteIdIngresado, meseroId = sessionUserId
     */
    fun placeOrder(
        sessionUserId: Long,
        sessionRole: Role,
        clienteIdForMesero: Long?
    ) {
        if (_ui.value.items.isEmpty()) {
            _ui.value = _ui.value.copy(error = "Tu carrito está vacío")
            return
        }

        val clienteId = when (sessionRole) {
            Role.CLIENTE -> sessionUserId
            Role.MESERO -> clienteIdForMesero
            else -> sessionUserId
        }

        val meseroId = if (sessionRole == Role.MESERO) sessionUserId else null

        if (clienteId == null) {
            _ui.value = _ui.value.copy(error = "Como mesero debes ingresar el clienteId")
            return
        }

        _ui.value = _ui.value.copy(loading = true, error = null, orderCreatedId = null)
        viewModelScope.launch {
            when (val res = createOrder(clienteId, meseroId, _ui.value.items)) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(loading = false, orderCreatedId = res.data.id)
                    clear()
                }
                is ApiResult.Error -> _ui.value = _ui.value.copy(loading = false, error = res.message)
            }
        }
    }

    fun consumeOrderCreated() {
        _ui.value = _ui.value.copy(orderCreatedId = null)
    }
}

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val orderCreatedId: Long? = null
)
