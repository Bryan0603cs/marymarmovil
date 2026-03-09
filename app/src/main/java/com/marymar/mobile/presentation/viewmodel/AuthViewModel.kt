package com.marymar.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marymar.mobile.core.network.toReadableMessage
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.api.AuthApi
import com.marymar.mobile.data.remote.dto.ForgotPasswordRequestDto
import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.domain.usecase.LoginUseCase
import com.marymar.mobile.domain.usecase.RegisterUseCase
import com.marymar.mobile.domain.usecase.ResendCodeUseCase
import com.marymar.mobile.domain.usecase.ValidateCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val loginUseCase: LoginUseCase,
    private val validateCodeUseCase: ValidateCodeUseCase,
    private val registerUseCase: RegisterUseCase,
    private val resendCodeUseCase: ResendCodeUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    fun setCaptchaToken(token: String) {
        val clean = token.trim()
        _ui.value = _ui.value.copy(
            captchaToken = clean,
            captchaVerified = clean.isNotBlank(),
            error = null,
            info = null
        )
    }

    fun clearCaptcha() {
        _ui.value = _ui.value.copy(
            captchaToken = "",
            captchaVerified = false
        )
    }

    fun clearBanners() {
        _ui.value = _ui.value.copy(
            error = null,
            info = null
        )
    }

    fun login(email: String, password: String) {
        val captchaToken = _ui.value.captchaToken.trim()

        if (email.isBlank() || password.isBlank()) {
            _ui.value = _ui.value.copy(error = "Completa el correo y la contraseña")
            return
        }

        if (captchaToken.isBlank()) {
            _ui.value = _ui.value.copy(error = "Completa el captcha")
            return
        }

        _ui.value = _ui.value.copy(
            loading = true,
            error = null,
            info = null,
            next = null
        )

        viewModelScope.launch {
            when (val res = loginUseCase(email.trim(), password, captchaToken)) {
                is ApiResult.Success -> {
                    val step = res.data
                    _ui.value = _ui.value.copy(
                        loading = false,
                        info = if (step.requires2FA) "Código enviado" else null,
                        next = if (step.requires2FA) {
                            AuthNext.GoToCode(email.trim())
                        } else {
                            AuthNext.LoggedIn
                        }
                    )
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        loading = false,
                        error = res.message
                    )
                }
            }
        }
    }

    fun validateCode(email: String, code: String) {
        _ui.value = _ui.value.copy(
            loading = true,
            error = null,
            info = null,
            next = null
        )

        viewModelScope.launch {
            when (val res = validateCodeUseCase(email, code)) {
                is ApiResult.Success -> {
                    if (res.data.role == Role.ADMINISTRADOR) {
                        _ui.value = _ui.value.copy(
                            loading = false,
                            error = "Este rol ingresa desde web"
                        )
                    } else {
                        _ui.value = _ui.value.copy(
                            loading = false,
                            next = AuthNext.LoggedIn
                        )
                    }
                }

                is ApiResult.Error -> {
                    val message = if (
                        res.code in listOf(400, 401, 403, 404) ||
                        res.message.contains("código", ignoreCase = true) ||
                        res.message.contains("codigo", ignoreCase = true) ||
                        res.message.contains("code", ignoreCase = true) ||
                        res.message.contains("invál", ignoreCase = true) ||
                        res.message.contains("invalid", ignoreCase = true)
                    ) {
                        "Código incorrecto"
                    } else {
                        "No fue posible validar el código"
                    }

                    _ui.value = _ui.value.copy(
                        loading = false,
                        error = message
                    )
                }
            }
        }
    }

    fun resendCode(email: String) {
        _ui.value = _ui.value.copy(
            loadingResend = true,
            error = null,
            info = null
        )

        viewModelScope.launch {
            when (val res = resendCodeUseCase(email)) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        loadingResend = false,
                        info = "Código reenviado"
                    )
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        loadingResend = false,
                        error = res.message
                    )
                }
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _ui.value = _ui.value.copy(error = "Correo no disponible")
            return
        }

        _ui.value = _ui.value.copy(
            loadingForgot = true,
            error = null,
            info = null
        )

        viewModelScope.launch {
            try {
                val response = authApi.forgotPassword(ForgotPasswordRequestDto(email.trim()))
                if (response.isSuccessful) {
                    _ui.value = _ui.value.copy(
                        loadingForgot = false,
                        info = "Revisa tu correo"
                    )
                } else {
                    _ui.value = _ui.value.copy(
                        loadingForgot = false,
                        error = "No fue posible enviar el correo"
                    )
                }
            } catch (e: HttpException) {
                _ui.value = _ui.value.copy(
                    loadingForgot = false,
                    error = e.toReadableMessage("No fue posible enviar el correo")
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    loadingForgot = false,
                    error = e.message ?: "Error inesperado"
                )
            }
        }
    }

    fun register(
        idNumber: String,
        name: String,
        email: String,
        password: String,
        phone: String,
        birthDateIso: String,
        role: Role,
        aceptaHabeasData: Boolean
    ) {
        val captchaToken = _ui.value.captchaToken.trim()

        if (captchaToken.isBlank()) {
            _ui.value = _ui.value.copy(error = "Completa el captcha")
            return
        }

        _ui.value = _ui.value.copy(
            loading = true,
            error = null,
            info = null,
            next = null
        )

        viewModelScope.launch {
            when (
                val res = registerUseCase(
                    idNumber = idNumber,
                    name = name,
                    email = email,
                    password = password,
                    phone = phone,
                    birthDateIso = birthDateIso,
                    role = role,
                    aceptaHabeasData = aceptaHabeasData,
                    captchaToken = captchaToken
                )
            ) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        loading = false,
                        next = AuthNext.LoggedIn
                    )
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        loading = false,
                        error = res.message
                    )
                }
            }
        }
    }

    fun onGoogleLoginRequested() {
        _ui.value = _ui.value.copy(
            error = null,
            info = null
        )
    }

    fun consumeNext() {
        _ui.value = _ui.value.copy(next = null)
    }
}

data class AuthUiState(
    val loading: Boolean = false,
    val loadingResend: Boolean = false,
    val loadingForgot: Boolean = false,
    val error: String? = null,
    val info: String? = null,
    val next: AuthNext? = null,
    val captchaToken: String = "",
    val captchaVerified: Boolean = false
)

sealed class AuthNext {
    data class GoToCode(val email: String) : AuthNext()
    data object LoggedIn : AuthNext()
}