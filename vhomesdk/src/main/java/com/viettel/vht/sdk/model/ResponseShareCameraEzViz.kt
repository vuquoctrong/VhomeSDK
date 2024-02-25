package com.viettel.vht.sdk.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResponseSharePendingCameraEzViz(
    @SerializedName("stop")
    var stop: Boolean = false,
    @SerializedName("meta")
    var meta: MetaEzViz? = null,
    @SerializedName("shareUserV2Responses")
    var shareUserV2Responses: List<ShareUserV2Response>? = null
)

data class MetaEzViz(
    @SerializedName("code")
    var code: Int = 0,
    @SerializedName("message")
    var message: String? = null,
)

data class ShareUserV2Response(
    @SerializedName("userShareId")
    var userShareId: Int? = null,
    @SerializedName("deviceShareId")
    var deviceShareId: Int? = null,
    @SerializedName("userId")
    var userId: String? = null,
    @SerializedName("phone")
    var phone: String? = null,
    @SerializedName("shareUserRemark")
    var shareUserRemark: String? = null,
    @SerializedName("shareCameras")
    var shareCameras: List<ShareCamera>? = listOf()
) : Serializable

data class ShareCamera(
    @SerializedName("cameraId")
    var cameraId: String? = null,
    @SerializedName("channelNo")
    var channelNo: Int = 1,
    @SerializedName("cameraName")
    var cameraName: String? = null,
    @SerializedName("shareTime")
    var shareTime: Long? = null,
    @SerializedName("permission")
    var permission: Int? = null
)

data class RequestAddDeviceIOT(
    @SerializedName("device_id")
    var deviceId: String,

    //receiver used
    @SerializedName("phone")
    var phone: String,

    @SerializedName("permission")
    var permission: String = "",

    @SerializedName("action")
    var action: String = "invite",
)

data class RequestUpdatePermissionDeviceIOT(
    @SerializedName("device_id")
    var deviceId: String,

    //receiver used
    @SerializedName("phone")
    var phone: String,

    @SerializedName("permission")
    var permission: String = "",

    @SerializedName("action")
    var action: String = "owner_update",
)

data class RequestDeleteSharePendingDeviceIOT(
    @SerializedName("device_id")
    var deviceId: String,

    //receiver used
    @SerializedName("phone")
    var phone: String,

    @SerializedName("action")
    var action: String = "revoke",
)

data class RequestDeleteShareAcceptedDeviceIOT(
    @SerializedName("device_id")
    var deviceId: String,

    //receiver used
    @SerializedName("phone")
    var phone: String,

    @SerializedName("action")
    var action: String = "delete",
)

data class RequestAcceptedShareDeviceIOT(
    @SerializedName("device_id")
    var deviceId: String,

    //receiver used
    @SerializedName("org_id")
    var orgId: String,

    @SerializedName("action")
    var action: String = "accept",
)


data class RequestListSharingDevice(
    @SerializedName("type")
    var type: String = "shareTo",
    // để chống để lấy all, accepted:để chấp nhận, peding: đang chờ
    @SerializedName("status")
    var status: String = "",
    @SerializedName("limit")
    var limit: Int = 100,
    @SerializedName("offset")
    var offset: Int = 0,
)

data class RequestListReceiveDevice(
    @SerializedName("type")
    var type: String = "shareFrom",
    @SerializedName("status")
    var status: String = "",
    @SerializedName("limit")
    var limit: Int = 100,
    @SerializedName("offset")
    var offset: Int = 0,
    @SerializedName("device_id")
    var deviceId: String = ""
)

data class RequestAddCameraEzViz(

    var email: String = "",
    var phone: String,
    var remark: String = "remark",
    var shareDeviceInfos: List<ShareDeviceInfos> = listOf()
)

data class ResponseAddCameraEzViz(
    @SerializedName("resultCode")
    var resultCode: String? = null,
    @SerializedName("resultDes")
    var resultDes: String? = null,
)

data class ResponseDeleteSharePendingEzViz(
    @SerializedName("meta")
    var meta: MetaSharePending? = null,

    )

data class ResponseAcceptedReceiveEzViz(
    @SerializedName("resultCode")
    var resultCode: String? = null,
    )

data class ResponseDeletedReceiveEzViz(
    @SerializedName("meta")
    var meta: MetaSharePending? = null,
    )

data class ResponseRejectReceiveEzViz(
    @SerializedName("resultCode")
    var resultCode: String? = null,

    )

data class MetaSharePending(
    @SerializedName("code")
    var code: Int,
    @SerializedName("message")
    var message: String? = null,
)

data class ShareDeviceInfos(
    var subSerial: String,
    var permission: Int,
    var shareCameras: List<ShareCameras> = listOf()

)

data class ShareCameras(
    var channelNo: Int = 1,
    var permission: Int
)

data class ResponseListSharePendingReceived(
    @SerializedName("resultCode")
    var resultCode: String? = null,
    @SerializedName("shareInfos")
    var shareInfos: List<ShareInfoReceive> = listOf()
)

data class ResponseListAcceptedShareReceived(
    @SerializedName("resultCode")
    var resultCode: String? = null,
    @SerializedName("shareDevices")
    var shareDevices: List<ShareAcceptedDevices> = listOf()
)

data class ShareAcceptedDevices(
    @SerializedName("deviceId")
    var deviceId: Long?= null,
    @SerializedName("subSerial")
    var subSerial: String?= null,
    @SerializedName("owner")
    var owner: String?= null,
    @SerializedName("permission")
    var permission: Int?= null,
    @SerializedName("devDomain")
    var devDomain: String?= null,
    @SerializedName("devName")
    var devName: String?= null,
    @SerializedName("devType")
    var devType: String?= null,
    @SerializedName("createTime")
    var createTime: Long?= null,
    @SerializedName("shareCameras")
    var shareCameras: List<ShareCamerasAccepted> = listOf()
)
data class ShareCamerasAccepted(
    @SerializedName("cameraId")
    var cameraId: String?= null,

    @SerializedName("cameraName")
    var cameraName: String?= null,

    @SerializedName("permission")
    var permission: Int?= null,

    @SerializedName("shareTime")
    var shareTime: Long?= null,
)
data class ShareInfoReceive(
    @SerializedName("shareId")
    var shareId: Long?= null,
    @SerializedName("subSerial")
    var subSerial: String?= null,
    @SerializedName("deviceName")
    var deviceName: String?= null,
    @SerializedName("owner")
    var owner: String?= null,
    @SerializedName("shareTime")
    var shareTime: String?= null
)

data class ResponseCheckPhone(
    @SerializedName("code")
    var code: Int?= null,

    @SerializedName("message")
    var message: String?= null,

    @SerializedName("jf_user")
    var jfUser: String?= null,
    @SerializedName("user_id")
    var userId: String?= null,
)

data class ResponseCheckDeviceSpread(
    @SerializedName("code")
    var code: Int?= null,

    @SerializedName("error")
    var error: String?= null,

    @SerializedName("data")
    var data: String?= null,

    @SerializedName("message")
    var message: String?= null,
)

