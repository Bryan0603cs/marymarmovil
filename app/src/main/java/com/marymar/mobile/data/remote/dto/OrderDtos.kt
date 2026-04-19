package com.marymar.mobile.data.remote.dto

import com.squareup.moshi.Json

data class OrderCreateDto(
    @Json(name = "clienteId") val clienteId: Long? = null,
    @Json(name = "meseroId") val meseroId: Long? = null,
    @Json(name = "mesaId") val mesaId: Long? = null,
    @Json(name = "tipo") val tipo: String,
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
    @Json(name = "tipo") val tipo: String,
    @Json(name = "clienteNombre") val clienteNombre: String?,
    @Json(name = "meseroId") val meseroId: Long?,
    @Json(name = "meseroNombre") val meseroNombre: String?,
    @Json(name = "mesaId") val mesaId: Long?,
    @Json(name = "numeroMesa") val numeroMesa: Int?,
    @Json(name = "total") val total: Double,
    @Json(name = "detalles") val detalles: List<OrderDetailResponseDto>?,
    @Json(name = "pago") val pago: PaymentResponseDto? = null
)

data class OrderDetailResponseDto(
    @Json(name = "id") val id: Long,
    @Json(name = "productoNombre") val productoNombre: String,
    @Json(name = "cantidad") val cantidad: Int,
    @Json(name = "precioUnitario") val precioUnitario: Double,
    @Json(name = "subtotal") val subtotal: Double,
    @Json(name = "productoId") val productoId: Long
)

data class PaymentResponseDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "metodo") val metodo: String? = null,
    @Json(name = "monto") val monto: Double? = null,
    @Json(name = "fechaPago") val fechaPago: String? = null,
    @Json(name = "comprobanteUrl") val comprobanteUrl: String? = null
)

data class TableResponseDto(
    @Json(name = "id") val id: Long,
    @Json(name = "numero") val numero: Int,
    @Json(name = "capacidad") val capacidad: Int?,
    @Json(name = "estado") val estado: String,
    @Json(name = "meseroAsignadoId") val meseroAsignadoId: Long?,
    @Json(name = "meseroAsignadoNombre") val meseroAsignadoNombre: String?,
    @Json(name = "activa") val activa: Boolean
)