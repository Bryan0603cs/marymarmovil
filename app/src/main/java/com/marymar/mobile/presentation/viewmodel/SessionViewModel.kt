package com.marymar.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marymar.mobile.core.storage.SessionStore
import com.marymar.mobile.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val sessionStore: SessionStore
) : ViewModel() {

    suspend fun performLogout() {
        logoutUseCase()
    }

    fun updateProfile(
        name: String,
        email: String,
        phone: String,
        address: String
    ) {
        viewModelScope.launch {
            sessionStore.updateProfile(
                name = name,
                email = email,
                phone = phone,
                address = address
            )
        }
    }
}