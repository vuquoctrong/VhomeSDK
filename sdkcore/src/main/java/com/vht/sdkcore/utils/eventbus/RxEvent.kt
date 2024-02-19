package com.viettel.vht.core.utils.eventbus

data class RxEvent<T>(val keyId: String, val value: T? = null)
