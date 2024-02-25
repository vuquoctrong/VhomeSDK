package com.viettel.vht.sdk.model.camera_cloud

data class CloudStoragePackageResponse(
    val total: Int?,
    val limit: Int?,
    val offset: Int?,
    val data : List<CloudStoragePackage>?,
)

data class CloudStoragePackage(
    var id: String = "",
    var name: String? = null,
    var code: String? = null,
    var type: String? = null,
    var method: Int? = null,
    var price: Long? = null,
    var period: Int? = null,
    var expired: Long? = null,
    var vendor: String? = null,
    var descriptions: Descriptions? = null,
    var status: Int? = null,
    var isSelected: Boolean = false,
)