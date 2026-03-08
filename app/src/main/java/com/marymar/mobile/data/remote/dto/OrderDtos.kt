package com.marymar.mobile.data.remote.dto

import com.squareup.moshi.Json

data class OrderCreateDto(
    @Json(name = "clienteId") val clienteId: Long,
    @Json(name = "meseroId") val meseroId: Long? = null,
    @Json(name = "detalles") val detalles: List<OrderDetailCreateDto>
)

data class OrderDetailCreateDto(
    @Json(name = "productoId") val productoId: Long,
    @Json(name = "cantidad") val cantidad: Int
)

data class OrderResponseDto(
    @Json(name = "id") val id: Long,
    @Json(name = "fecha") val fecha: String,
    @Json(name = "estado") val estado: String,
    @Json(name = "clienteNombre") val clienteNombre: String?,
    @Json(name = "meseroNombre") val meseroNombre: String?,
    @Json(name = "total") val total: Double,
    @Json(name = "detalles") val detalles: List<OrderDetailResponseDto>?
)

data class OrderDetailResponseDto(
    @Json(name = "id") val id: Long,
    @Json(name = "productoNombre") val productoNombre: String,
    @Json(name = "cantidad") val cantidad: Int,
    @Json(name = "precioUnitario") val precioUnitario: Double,
    @Json(name = "subtotal") val subtotal: Double
)
