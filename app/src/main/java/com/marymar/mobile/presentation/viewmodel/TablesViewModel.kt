package com.marymar.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.dto.TableResponseDto
import com.marymar.mobile.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
        _ui.value = _ui.value.copy(loading = true, error = null)
        viewModelScope.launch {
            when (val result = orderRepository.getTables()) {
                is ApiResult.Success -> _ui.value = _ui.value.copy(loading = false, tables = result.data)
                is ApiResult.Error -> _ui.value = _ui.value.copy(loading = false, error = result.message)
            }
        }
    }

    fun openTable(tableId: Long, meseroId: Long, onSuccess: () -> Unit) {
        _ui.value = _ui.value.copy(actionLoadingTableId = tableId, error = null)
        viewModelScope.launch {
            when (val result = orderRepository.openTable(tableId, meseroId)) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        actionLoadingTableId = null,
                        message = "Mesa ${result.data.numero} abierta correctamente"
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

    fun cancelTable(tableId: Long) {
        _ui.value = _ui.value.copy(actionLoadingTableId = tableId, error = null)
        viewModelScope.launch {
            when (val result = orderRepository.cancelTable(tableId)) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        actionLoadingTableId = null,
                        message = "Mesa ${result.data.numero} cancelada y liberada"
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
}

data class TablesUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val actionLoadingTableId: Long? = null,
    val tables: List<TableResponseDto> = emptyList()
)
