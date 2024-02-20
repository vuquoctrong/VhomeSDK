package com.viettel.vht.sdk.model.login

import com.google.gson.annotations.SerializedName

data class VersionUpdateResponse(

    @SerializedName("total")
    var total: Int? = null,

    @SerializedName("limit")
    var limit: Int? = null,

    @SerializedName("offset")
    var offset: Int? = null,

    @SerializedName("data")
    var data: List<VersionUpdate>? = null,

    )
data class VersionUpdate(

    @SerializedName("id")
    var id: Int? = null,

    @SerializedName("os_app")
    var osApp: String? = null,

    @SerializedName("version")
    var version: String? = null,

    @SerializedName("link")
    var link: String? = null,

    @SerializedName("description")
    var description: String? = null,

    @SerializedName("force_update")
    var forceUpdate: String? = null,

    @SerializedName("route_domain")
    var routeDomain: String? = null,

    @SerializedName("created_time")
    var createdTime: Long? = null,

    @SerializedName("update_time")
    var update_time: Long? = null,
)

enum class ForceUpdate(var data :String){
    NONE("NONE"),
    INFO("INFO"),
    UPDATE("UPDATE"),

}
