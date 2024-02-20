package com.vht.sdkcore.utils.eventbus

data class RxEvent<T>(val keyId: String, val value: T? = null)
