package com.marymar.mobile.domain.usecase

import com.marymar.mobile.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repo: ProductRepository
) {
    suspend operator fun invoke() = repo.getProducts()
}
