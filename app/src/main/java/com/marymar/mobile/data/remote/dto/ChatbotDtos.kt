package com.marymar.mobile.data.remote.dto

import com.squareup.moshi.Json

data class ChatbotRequestDto(
    @Json(name = "mensaje") val mensaje: String
)

data class ChatbotResponseDto(
    @Json(name = "respuesta") val respuesta: String
)