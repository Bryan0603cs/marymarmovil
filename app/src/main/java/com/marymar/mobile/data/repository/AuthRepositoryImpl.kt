package com.marymar.mobile.data.repository

import com.marymar.mobile.core.network.TokenProvider
import com.marymar.mobile.core.network.toReadableMessage
import com.marymar.mobile.core.storage.SessionStore
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.api.AuthApi
import com.marymar.mobile.data.remote.dto.LoginRequestDto
import com.marymar.mobile.data.remote.dto.RegisterRequestDto
import com.marymar.mobile.data.remote.dto.ResendCodeRequestDto
import com.marymar.mobile.data.remote.dto.VerifyTokenRequestDto
import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.domain.model.Session
import com.marymar.mobile.domain.repository.AuthRepository
import com.marymar.mobile.domain.repository.LoginStep
import javax.inject.Inject
import retrofit2.HttpException

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionStore: SessionStore,
    private val tokenProvider: TokenProvider
) : AuthRepository {

    override suspend fun register(
        idNumber: String,
        name: String,
        email: String,
        password: String,
        phone: String,
        birthDateIso: String,
        role: Role,
        aceptaHabeasData: Boolean,
        captchaToken: String
    ): ApiResult<Session> {
        return try {
            val resp = api.register(
                RegisterRequestDto(
                    numeroIdentificacion = idNumber,
                    nombre = name,
                    email = email,
                    contrasena = password,
                    telefono = phone,
                    fechaNacimiento = birthDateIso,
                    rol = role.name,
                    aceptaHabeasData = aceptaHabeasData,
                    captchaToken = captchaToken
                )
            )

            val token = resp.token
                ?: return ApiResult.Error(resp.mensaje ?: "No se recibió token en el registro")

            val persona = api.verifyToken(
                VerifyTokenRequestDto(token)
            )

            val session = Session(
                token = token,
                userId = persona.id,
                email = persona.email,
                name = resp.nombre ?: persona.nombre,
                role = Role.valueOf(persona.rol)
            )

            sessionStore.saveSession(
                token = session.token,
                email = session.email,
                name = session.name,
                role = session.role.name,
                userId = session.userId,
                phone = persona.telefono,
                address = persona.direccionEnvio,
                birthDate = persona.fechaNacimiento,
                idNumber = persona.numeroIdentificacion
            )

            tokenProvider.setToken(session.token)
            ApiResult.Success(session)
        } catch (e: HttpException) {
            ApiResult.Error(
                e.toReadableMessage("No fue posible crear la cuenta"),
                e.code(),
                e
            )
        } catch (e: Exception) {
            ApiResult.Error(
                e.message ?: "Error inesperado al registrar la cuenta",
                null,
                e
            )
        }
    }

    override suspend fun login(
        email: String,
        password: String,
        captchaToken: String
    ): ApiResult<LoginStep> {
        return try {
            val resp = api.login(
                LoginRequestDto(
                    email = email,
                    contrasena = password,
                    captchaToken = captchaToken
                )
            )

            ApiResult.Success(
                LoginStep(
                    requires2FA = resp.requires2FA ?: false,
                    message = resp.mensaje
                )
            )
        } catch (e: HttpException) {
            ApiResult.Error(
                e.toReadableMessage("No fue posible iniciar sesión"),
                e.code(),
                e
            )
        } catch (e: Exception) {
            ApiResult.Error(
                e.message ?: "Error inesperado al iniciar sesión",
                null,
                e
            )
        }
    }

    override suspend fun validateCode(
        email: String,
        code: String
    ): ApiResult<Session> {
        return try {
            val resp = api.validateCode(email = email, code = code)

            val token = resp.token
                ?: return ApiResult.Error(resp.mensaje ?: "Código inválido o sin token")

            val persona = api.verifyToken(
                VerifyTokenRequestDto(token)
            )

            val session = Session(
                token = token,
                userId = persona.id,
                email = persona.email,
                name = resp.nombre ?: persona.nombre,
                role = Role.valueOf(persona.rol)
            )

            sessionStore.saveSession(
                token = session.token,
                email = session.email,
                name = session.name,
                role = session.role.name,
                userId = session.userId,
                phone = persona.telefono,
                address = persona.direccionEnvio,
                birthDate = persona.fechaNacimiento,
                idNumber = persona.numeroIdentificacion
            )

            tokenProvider.setToken(session.token)
            ApiResult.Success(session)
        } catch (e: HttpException) {
            ApiResult.Error(
                e.toReadableMessage("No fue posible validar el código"),
                e.code(),
                e
            )
        } catch (e: Exception) {
            ApiResult.Error(
                e.message ?: "Error inesperado al validar el código",
                null,
                e
            )
        }
    }

    override suspend fun resendCode(email: String): ApiResult<String> {
        return try {
            val response = api.resendCode(
                ResendCodeRequestDto(email)
            )

            ApiResult.Success(
                response.mensaje ?: "Código reenviado correctamente"
            )
        } catch (e: HttpException) {
            ApiResult.Error(
                e.toReadableMessage("No fue posible reenviar el código"),
                e.code(),
                e
            )
        } catch (e: Exception) {
            ApiResult.Error(
                e.message ?: "Error inesperado al reenviar el código",
                null,
                e
            )
        }
    }

    override suspend fun logout() {
        tokenProvider.setToken(null)
        sessionStore.clear()
    }
}