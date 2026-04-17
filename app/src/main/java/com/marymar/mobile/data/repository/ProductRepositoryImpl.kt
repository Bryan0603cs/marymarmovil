package com.marymar.mobile.data.repository

import com.marymar.mobile.core.network.toReadableMessage
import com.marymar.mobile.core.network.toUserFriendlyMessage
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.api.ProductApi
import com.marymar.mobile.domain.model.Product
import com.marymar.mobile.domain.repository.ProductRepository
import retrofit2.HttpException
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: ProductApi
) : ProductRepository {

    override suspend fun getProducts(): ApiResult<List<Product>> {
        return try {
            val dtos = api.getProducts()
            val products = dtos
                .filter { it.activo }
                .map { dto ->
                    Product(
                        id = dto.id,
                        name = dto.nombre,
                        price = dto.precio,
                        description = dto.descripcion,
                        category = dto.categoriaNombre,
                        active = dto.activo,
                        imageUrl = dto.imagenPrincipal ?: dto.imagenesUrls?.firstOrNull()
                    )
                }
            ApiResult.Success(products)
        } catch (e: HttpException) {
            ApiResult.Error(e.toReadableMessage("No fue posible cargar el menú"), e.code(), e)
        } catch (e: Exception) {
            ApiResult.Error(e.toUserFriendlyMessage("No fue posible cargar el menú"), null, e)
        }
    }
}