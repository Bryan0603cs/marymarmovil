package com.marymar.mobile.data.repository

import com.marymar.mobile.core.network.toReadableMessage
import com.marymar.mobile.core.network.toUserFriendlyMessage
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.api.ChatbotApi
import com.marymar.mobile.data.remote.dto.ChatbotRequestDto
import com.marymar.mobile.domain.repository.ChatbotRepository
import javax.inject.Inject
import retrofit2.HttpException

class ChatbotRepositoryImpl @Inject constructor(
    private val api: ChatbotApi
) : ChatbotRepository {

    override suspend fun ask(message: String): ApiResult<String> {
        return try {
            val response = api.ask(ChatbotRequestDto(mensaje = message))
            ApiResult.Success(response.respuesta)
        } catch (e: HttpException) {
            ApiResult.Error(
                e.toReadableMessage("No fue posible contactar al asistente"),
                e.code(),
                e
            )
        } catch (e: Exception) {
            ApiResult.Error(
                e.toUserFriendlyMessage("No fue posible contactar al asistente"),
                null,
                e
            )
        }
    }
}