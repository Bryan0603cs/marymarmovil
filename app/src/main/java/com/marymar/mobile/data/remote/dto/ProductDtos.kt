package com.marymar.mobile.data.remote.dto

import com.squareup.moshi.Json

data class ProductResponseDto(
    @Json(name = "id") val id: Long,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "precio") val precio: Double,
    @Json(name = "descripcion") val descripcion: String? = null,
    @Json(name = "categoriaNombre") val categoriaNombre: String?,
    @Json(name = "activo") val activo: Boolean,
    @Json(name = "imagenesUrls") val imagenesUrls: List<String>? = null,
    @Json(name = "imagenPrincipal") val imagenPrincipal: String? = null
)
