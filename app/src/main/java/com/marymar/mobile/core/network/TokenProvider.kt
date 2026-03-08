package com.marymar.mobile.core.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenProvider @Inject constructor() {
    @Volatile private var _token: String? = null

    fun setToken(token: String?) {
        _token = token
    }

    fun getToken(): String? = _token
}
