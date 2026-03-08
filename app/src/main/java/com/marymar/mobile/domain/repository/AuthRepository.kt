package com.marymar.mobile.domain.repository

import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.domain.model.Session

interface AuthRepository {
    suspend fun register(
        idNumber: String,
        name: String,
        email: String,
        password: String,
        phone: String,
        birthDateIso: String,
        role: Role,
        aceptaHabeasData: Boolean,
        captchaToken: String
    ): ApiResult<Session>

    suspend fun login(
        email: String,
        password: String,
        captchaToken: String
    ): ApiResult<LoginStep>

    suspend fun validateCode(email: String, code: String): ApiResult<Session>
    suspend fun resendCode(email: String): ApiResult<String>
    suspend fun logout()
}

data class LoginStep(
    val requires2FA: Boolean,
    val message: String?
)