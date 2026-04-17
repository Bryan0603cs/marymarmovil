package com.marymar.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marymar.mobile.core.network.toReadableMessage
import com.marymar.mobile.core.network.toUserFriendlyMessage
import com.marymar.mobile.core.security.NativeRecaptchaManager
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.api.AuthApi
import com.marymar.mobile.data.remote.dto.ForgotPasswordRequestDto
import com.marymar.mobile.domain.model.AuthAction
import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.domain.usecase.LoginUseCase
import com.marymar.mobile.domain.usecase.LoginWithGoogleUseCase
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
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val validateCodeUseCase: ValidateCodeUseCase,
    private val registerUseCase: RegisterUseCase,
    private val resendCodeUseCase: ResendCodeUseCase,
    private val nativeRecaptchaManager: NativeRecaptchaManager
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    fun clearBanners() {
        _ui.value = _ui.value.copy(
            error = null,
            info = null
        )
    }

    fun startGoogleLoginFlow() {
        _ui.value = _ui.value.copy(
            loadingGoogle = true,
            error = null,
            info = null,
            next = null
        )
    }

    fun cancelGoogleLogin(message: String? = null) {
        _ui.value = _ui.value.copy(
            loadingGoogle = false,
            error = message,
            info = null
        )
    }

    fun login(email: String, password: String) {
        val cleanEmail = email.trim()

        if (cleanEmail.isBlank() || password.isBlank()) {
            _ui.value = _ui.value.copy(error = "Completa el correo y la contraseña")
            return
        }

        _ui.value = _ui.value.copy(
            loading = true,
            error = null,
            info = null,
            next = null
        )

        viewModelScope.launch {
            val captchaToken = nativeRecaptchaManager.execute(AuthAction.LOGIN)
                .getOrElse { throwable ->
                    _ui.value = _ui.value.copy(
                        loading = false,
                        error = recaptchaErrorMessage(throwable)
                    )
                    return@launch
                }

            when (val res = loginUseCase(cleanEmail, password, captchaToken, AuthAction.LOGIN)) {
                is ApiResult.Success -> {
                    val step = res.data
                    _ui.value = _ui.value.copy(
                        loading = false,
                        info = if (step.requires2FA) "Código enviado" else null,
                        next = if (step.requires2FA) {
                            AuthNext.GoToCode(cleanEmail)
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

    fun loginWithGoogle(idToken: String) {
        if (idToken.isBlank()) {
            _ui.value = _ui.value.copy(
                loadingGoogle = false,
                error = "Google no devolvió un token válido"
            )
            return
        }

        _ui.value = _ui.value.copy(
            loadingGoogle = true,
            error = null,
            info = null,
            next = null
        )

        viewModelScope.launch {
            val captchaToken = nativeRecaptchaManager.execute(AuthAction.GOOGLE_LOGIN)
                .getOrElse { throwable ->
                    _ui.value = _ui.value.copy(
                        loadingGoogle = false,
                        error = recaptchaErrorMessage(throwable)
                    )
                    return@launch
                }

            when (
                val res = loginWithGoogleUseCase(
                    idToken = idToken,
                    captchaToken = captchaToken,
                    captchaAction = AuthAction.GOOGLE_LOGIN
                )
            ) {
                is ApiResult.Success -> {
                    if (res.data.role == Role.ADMINISTRADOR) {
                        _ui.value = _ui.value.copy(
                            loadingGoogle = false,
                            error = "Este rol ingresa desde web"
                        )
                    } else {
                        _ui.value = _ui.value.copy(
                            loadingGoogle = false,
                            next = AuthNext.LoggedIn
                        )
                    }
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        loadingGoogle = false,
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
                    error = e.toUserFriendlyMessage("No fue posible enviar el correo")
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
        if (!aceptaHabeasData) {
            _ui.value = _ui.value.copy(error = "Debes aceptar la Política de Tratamiento de Datos")
            return
        }

        _ui.value = _ui.value.copy(
            loading = true,
            error = null,
            info = null,
            next = null
        )

        viewModelScope.launch {
            val captchaToken = nativeRecaptchaManager.execute(AuthAction.REGISTER)
                .getOrElse { throwable ->
                    _ui.value = _ui.value.copy(
                        loading = false,
                        error = recaptchaErrorMessage(throwable)
                    )
                    return@launch
                }

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
                    captchaToken = captchaToken,
                    captchaAction = AuthAction.REGISTER
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

    fun consumeNext() {
        _ui.value = _ui.value.copy(next = null)
    }

    private fun recaptchaErrorMessage(throwable: Throwable): String {
        val raw = throwable.message.orEmpty()
        return when {
            raw.contains("network", ignoreCase = true) || raw.contains("timeout", ignoreCase = true) ->
                "No fue posible validar la seguridad de la app. Revisa tu conexión e inténtalo de nuevo."
            else -> "No fue posible validar la seguridad de la app. Intenta nuevamente."
        }
    }
}

data class AuthUiState(
    val loading: Boolean = false,
    val loadingGoogle: Boolean = false,
    val loadingResend: Boolean = false,
    val loadingForgot: Boolean = false,
    val error: String? = null,
    val info: String? = null,
    val next: AuthNext? = null
)

sealed class AuthNext {
    data class GoToCode(val email: String) : AuthNext()
    data object LoggedIn : AuthNext()
}