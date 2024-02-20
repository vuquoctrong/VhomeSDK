package com.viettel.vht.sdk.model.login

data class LoginRequest (
    var lang: String = " ",
    var countryCode: String =" ",
    var email: String =" ",
    var phoneNumber: String = " ",
    var password: String = " "
)