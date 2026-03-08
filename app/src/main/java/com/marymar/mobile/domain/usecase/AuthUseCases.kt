package com.marymar.mobile.domain.usecase

import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
        captchaToken: String
    ) = repo.login(email, password, captchaToken)
}

class ValidateCodeUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, code: String) = repo.validateCode(email, code)
}

class ResendCodeUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String) = repo.resendCode(email)
}

class RegisterUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(
        idNumber: String,
        name: String,
        email: String,
        password: String,
        phone: String,
        birthDateIso: String,
        role: Role,
        aceptaHabeasData: Boolean,
        captchaToken: String
    ) = repo.register(
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
}

class LogoutUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.logout()
}