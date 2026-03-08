package com.marymar.mobile.data.remote.api

import com.marymar.mobile.data.remote.dto.AuthResponseDto
import com.marymar.mobile.data.remote.dto.ForgotPasswordRequestDto
import com.marymar.mobile.data.remote.dto.LoginRequestDto
import com.marymar.mobile.data.remote.dto.PersonaResponseDto
import com.marymar.mobile.data.remote.dto.RegisterRequestDto
import com.marymar.mobile.data.remote.dto.ResendCodeRequestDto
import com.marymar.mobile.data.remote.dto.VerifyTokenRequestDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequestDto): AuthResponseDto

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("api/auth/validate-code")
    suspend fun validateCode(
        @Query("email") email: String,
        @Query("code") code: String
    ): AuthResponseDto

    @POST("api/auth/resend-code")
    suspend fun resendCode(@Body body: ResendCodeRequestDto): AuthResponseDto

    @POST("api/auth/verify-token")
    suspend fun verifyToken(@Body body: VerifyTokenRequestDto): PersonaResponseDto

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequestDto): Response<ResponseBody>
}