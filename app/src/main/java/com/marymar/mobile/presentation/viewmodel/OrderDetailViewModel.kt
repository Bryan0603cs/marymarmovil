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
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(OrderDetailUiState())
    val ui: StateFlow<OrderDetailUiState> = _ui.asStateFlow()

    fun load(orderId: Long) {
        _ui.value = _ui.value.copy(loading = true, error = null)
        viewModelScope.launch {
            when (val result = orderRepository.getOrder(orderId)) {
                is ApiResult.Success -> _ui.value = _ui.value.copy(loading = false, order = result.data)
                is ApiResult.Error -> _ui.value = _ui.value.copy(loading = false, error = result.message)
            }
        }
    }
}

data class OrderDetailUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val order: OrderResponseDto? = null
)
