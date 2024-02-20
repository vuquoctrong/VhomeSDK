package com.viettel.vht.sdk.model.login

data class UserResponse(
    val countryCode: String,
    val phoneNumber: String,
    val email: String,
    val apikey: String,
    val nickname: String,
    val wxServiceId: String,
    val wxAppId: String,
    val wxId: String,
    val wxOpenId: String,
    val yanKanYunInfo: Any,
    val accountLevel: Int,
    val levelExpiredAt: Long,
    val denyRecharge: Boolean,
    val accountConsult: Boolean,
    val ipCountry: String
)