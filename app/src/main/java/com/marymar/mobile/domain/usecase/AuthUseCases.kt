package com.marymar.mobile.domain.usecase

import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        captchaToken: String,
        captchaAction: String
    ) = repo.login(
        email = email,
        password = password,
        captchaToken = captchaToken,
        captchaAction = captchaAction
    )
}

class LoginWithGoogleUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(
        idToken: String,
        captchaToken: String,
        captchaAction: String
    ) = repo.loginWithGoogle(
        idToken = idToken,
        captchaToken = captchaToken,
        captchaAction = captchaAction
    )
}

class ValidateCodeUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String) =
        repo.validateCode(email, code)
}

class ResendCodeUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(email: String) =
        repo.resendCode(email)
}

class RegisterUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(
        idNumber: String,
        name: String,
        email: String,
        password: String,
        phone: String,
        birthDateIso: String,
        role: Role,
        aceptaHabeasData: Boolean,
        captchaToken: String,
        captchaAction: String
    ) = repo.register(
        idNumber = idNumber,
        name = name,
        email = email,
        password = password,
        phone = phone,
        birthDateIso = birthDateIso,
        role = role,
        aceptaHabeasData = aceptaHabeasData,
        captchaToken = captchaToken,
        captchaAction = captchaAction
    )
}

class LogoutUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke() = repo.logout()
}