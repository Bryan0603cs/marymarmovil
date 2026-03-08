package com.marymar.mobile.domain.repository

import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(): ApiResult<List<Product>>
}
