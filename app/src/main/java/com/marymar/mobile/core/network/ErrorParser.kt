package com.marymar.mobile.core.network

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
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

fun Throwable.toUserFriendlyMessage(defaultMessage: String): String {
    return when (this) {
        is UnknownHostException -> "No se pudo conectar con el servidor. Revisa internet y la URL base de la app."
        is SocketTimeoutException -> "El servidor tardó demasiado en responder. Intenta nuevamente."
        is SSLException -> "No fue posible establecer una conexión segura con el servidor."
        is JsonEncodingException,
        is JsonDataException -> "El servidor devolvió una respuesta no válida. Verifica que BASE_URL use HTTPS y que el backend responda JSON."
        else -> {
            val message = message.orEmpty()
            when {
                message.contains("BEGIN_OBJECT") || message.contains("BEGIN_ARRAY") ->
                    "La respuesta del servidor no coincide con lo esperado por la app."
                message.contains("malformed JSON", ignoreCase = true) ->
                    "El servidor devolvió contenido no válido. Revisa que la app esté apuntando al endpoint HTTPS correcto."
                else -> defaultMessage
            }
        }
    }
}