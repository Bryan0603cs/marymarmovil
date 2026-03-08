package com.marymar.mobile.core.util

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null, val throwable: Throwable? = null) : ApiResult<Nothing>()
}
