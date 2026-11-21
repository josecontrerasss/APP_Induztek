package com.example.app_induztek.data.remote

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class HttpError(val code: Int, val mensaje: String) : ApiResult<Nothing>
    data class NetworkError(val causa: Throwable) : ApiResult<Nothing>
}
