package com.vht.sdkcore.network

sealed class Result<T>(
    val data: T?,
    val exception: String?,
    val status: Status
) {
    class Loading<T> : Result<T>(null, null, Status.LOADING)
    class Error<T>(exception: String) : Result<T>(null, exception, Status.ERROR)
    class Success<T>(data: T?) : Result<T>(data, null, Status.SUCCESS)
}

enum class Status {
    LOADING,
    SUCCESS,
    ERROR
}