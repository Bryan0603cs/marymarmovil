package com.marymar.mobile.core.network

import org.json.JSONObject
import retrofit2.HttpException

fun HttpException.toReadableMessage(defaultMessage: String): String {
    return try {
        val raw = response()?.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return "$defaultMessage (HTTP ${code()})"
        val json = JSONObject(raw)
        when {
            json.has("message") -> json.getString("message")
            json.has("mensaje") -> json.getString("mensaje")
            json.has("error") -> json.getString("error")
            else -> "$defaultMessage (HTTP ${code()})"
        }
    } catch (_: Exception) {
        "$defaultMessage (HTTP ${code()})"
    }
}
