package com.marymar.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.domain.model.Product
import com.marymar.mobile.domain.usecase.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val getProducts: GetProductsUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(ProductsUiState())
    val ui: StateFlow<ProductsUiState> = _ui.asStateFlow()

    fun load() {
        _ui.value = _ui.value.copy(loading = true, error = null)
        viewModelScope.launch {
            when (val res = getProducts()) {
                is ApiResult.Success -> _ui.value = _ui.value.copy(
                    loading = false,
                    products = res.data,
                    categories = res.data.mapNotNull { it.category }.distinct().sorted()
                )
                is ApiResult.Error -> _ui.value = _ui.value.copy(loading = false, error = res.message)
            }
        }
    }

    fun setQuery(q: String) {
        _ui.value = _ui.value.copy(query = q)
    }

    fun setCategory(category: String?) {
        _ui.value = _ui.value.copy(selectedCategory = category)
    }

    fun filtered(): List<Product> {
        val q = _ui.value.query.trim().lowercase()
        return _ui.value.products.filter { product ->
            val matchesQuery = q.isBlank() ||
                product.name.lowercase().contains(q) ||
                (product.category?.lowercase()?.contains(q) == true) ||
                (product.description?.lowercase()?.contains(q) == true)
            val matchesCategory = _ui.value.selectedCategory == null || product.category == _ui.value.selectedCategory
            matchesQuery && matchesCategory
        }
    }
}

data class ProductsUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val query: String = ""
)
