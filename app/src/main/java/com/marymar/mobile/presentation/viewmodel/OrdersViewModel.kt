package com.marymar.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.dto.OrderResponseDto
import com.marymar.mobile.domain.usecase.GetOrdersByClientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val getOrdersByClient: GetOrdersByClientUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(OrdersUiState())
    val ui: StateFlow<OrdersUiState> = _ui.asStateFlow()

    fun load(clienteId: Long) {
        _ui.value = _ui.value.copy(loading = true, error = null)
        viewModelScope.launch {
            when (val res = getOrdersByClient(clienteId)) {
                is ApiResult.Success -> _ui.value = _ui.value.copy(loading = false, orders = res.data)
                is ApiResult.Error -> _ui.value = _ui.value.copy(loading = false, error = res.message)
            }
        }
    }
}

data class OrdersUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val orders: List<OrderResponseDto> = emptyList()
)
