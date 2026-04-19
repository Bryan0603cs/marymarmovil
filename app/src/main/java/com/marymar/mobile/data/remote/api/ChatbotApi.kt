package com.marymar.mobile.data.remote.api

import com.marymar.mobile.data.remote.dto.ChatbotRequestDto
import com.marymar.mobile.data.remote.dto.ChatbotResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatbotApi {

    @POST("api/chatbot")
    suspend fun ask(@Body body: ChatbotRequestDto): ChatbotResponseDto
}