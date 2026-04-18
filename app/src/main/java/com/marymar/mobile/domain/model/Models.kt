package com.marymar.mobile.domain.model

data class Session(
    val token: String,
    val userId: Long,
    val email: String,
    val name: String,
    val role: Role
)

enum class Role {
    CLIENTE,
    MESERO,
    COCINERO,
    ADMINISTRADOR
}

data class Product(
    val id: Long,
    val name: String,
    val price: Double,
    val description: String?,
    val category: String?,
    val active: Boolean,
    val imageUrl: String?
)

data class CartItem(
    val product: Product,
    val quantity: Int
)

data class Order(
    val id: Long,
    val date: String,
    val status: String,
    val total: Double
)