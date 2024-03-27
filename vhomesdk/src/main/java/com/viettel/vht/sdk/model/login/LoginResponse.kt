package com.viettel.vht.sdk.model.login

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LoginResponse(
    val code: Int = 0,
    val region: String,
    @SerializedName("token")
    val token: String = "",
    @SerializedName("device_token")
    val deviceToken: String = "",
    @SerializedName("ezviztoken")
    val ezvizToken: String,
    @SerializedName("area_domain")
    val areaDomain: String,
    @SerializedName("ez_token")
    val ezToken: String,
    @SerializedName("org_id")
    val orgId: String? = null,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("user_id")
    val userId: String = "",
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("accept_eula")
    val acceptEula: Boolean? = false,
    @SerializedName("third-party-info")
    val thirdPartyInfo: ThirdPartyInfo? = null
) : Serializable

data class ThirdPartyInfo(
    @SerializedName("ezviz-token")
    val ezToken: String = "",
    @SerializedName("jf-auth")
    val jfAuth: String = "",
    @SerializedName("jf-user")
    val jfUser: String = ""
)

data class RefreshTokenResponse(
    @SerializedName("token")
    val token: String = "",
    @SerializedName("jf-user")
    val jfUser: String = "",
    @SerializedName("jf-auth")
    val jfAuth: String = "",
    @SerializedName("phone")
    val phone: String = "",
    @SerializedName("code")
    val code: Int = -1,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("org_id")
    val orgId: String = "",
    @SerializedName("user_id")
    val userId: String = ""
)