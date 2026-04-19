package com.marymar.mobile.domain.repository

import com.marymar.mobile.core.util.ApiResult

interface ChatbotRepository {
    suspend fun ask(message: String): ApiResult<String>
}