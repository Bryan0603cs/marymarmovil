package com.marymar.mobile.data.remote.dto

import com.squareup.moshi.Json

data class LoginRequestDto(
    @Json(name = "email") val email: String,
    @Json(name = "contrasena") val contrasena: String,
    @Json(name = "captchaToken") val captchaToken: String
)

data class RegisterRequestDto(
    @Json(name = "numeroIdentificacion") val numeroIdentificacion: String,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "email") val email: String,
    @Json(name = "contrasena") val contrasena: String,
    @Json(name = "telefono") val telefono: String,
    @Json(name = "fechaNacimiento") val fechaNacimiento: String,
    @Json(name = "rol") val rol: String,
    @Json(name = "aceptaHabeasData") val aceptaHabeasData: Boolean,
    @Json(name = "captchaToken") val captchaToken: String
)

data class ForgotPasswordRequestDto(
    @Json(name = "email") val email: String
)

data class ResetPasswordRequestDto(
    @Json(name = "token") val token: String,
    @Json(name = "newPassword") val newPassword: String
)

data class AuthResponseDto(
    @Json(name = "token") val token: String? = null,
    @Json(name = "nombre") val nombre: String? = null,
    @Json(name = "rol") val rol: String? = null,
    @Json(name = "mensaje") val mensaje: String? = null,
    @Json(name = "requires2FA") val requires2FA: Boolean? = null
)

data class ResendCodeRequestDto(
    @Json(name = "email") val email: String
)

data class VerifyTokenRequestDto(
    @Json(name = "token") val token: String
)

data class PersonaResponseDto(
    @Json(name = "id") val id: Long,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "email") val email: String,
    @Json(name = "telefono") val telefono: String?,
    @Json(name = "fechaNacimiento") val fechaNacimiento: String?,
    @Json(name = "rol") val rol: String,
    @Json(name = "direccionEnvio") val direccionEnvio: String?,
    @Json(name = "salario") val salario: Double?,
    @Json(name = "numeroIdentificacion") val numeroIdentificacion: String?,
    @Json(name = "activo") val activo: Boolean,
    @Json(name = "createdAt") val createdAt: String?,
    @Json(name = "updatedAt") val updatedAt: String?
)