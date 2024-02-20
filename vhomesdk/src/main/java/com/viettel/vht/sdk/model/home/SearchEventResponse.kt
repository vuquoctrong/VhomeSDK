package com.viettel.vht.sdk.model.home

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SearchEventResponse(
    @SerializedName("errorCode")
    val errorCode: Int? = 0,
    @SerializedName("status")
    val status: Int? = 0,
    @SerializedName("errorName")
    val errorName: String = "",
    @SerializedName("data")
    val data: SearchEventData
)

data class ReadEventResponse(
    @SerializedName("errorCode")
    val errorCode: Int? = 0,
    @SerializedName("status")
    val status: Int? = 0,
    @SerializedName("errorName")
    val errorName: String = "",
    @SerializedName("data")
    val data: SearchEventItem? = null
)

data class SearchEventData(
    @SerializedName("currentPage")
    val currentPage: Int? = 0,
    @SerializedName("totalEvents")
    val totalEvents: Int? = 0,
    @SerializedName("events")
    var events: List<SearchEventItem>,
)

data class Description(
    @SerializedName("bboxes")
    var bboxes: MutableList<BBox>? = null,
    @SerializedName("msgId")
    var msgId: String? = null,
    @SerializedName("time")
    var time: Long = 0,
    @SerializedName("trackID")
    val trackID: Int = 0,
    @SerializedName("timeFromInitTrack")
    val timeFromInitTrack: Long = 0 //start time - end time
)

data class BBox(
    @SerializedName("alignRatio") var alignRatio: Double? = null,
    @SerializedName("conf") var conf: Double? = null,
    @SerializedName("distanceEyesRatio") var distanceEyesRatio: Int? = null,
    @SerializedName("eyeDistance") var eyeDistance: Double? = null,
    @SerializedName("eyeRatio") var eyeRatio: Double? = null,
    @SerializedName("faceProb") var faceProb: Double? = null,
    @SerializedName("feature") var feature: Any? = null,
    @SerializedName("featureDistUnknown") var featureDistUnknown: Double? = null,
    @SerializedName("finalProb") var finalProb: Double? = null,
    @SerializedName("fullName") var fullName: String? = null,
    @SerializedName("index") var index: Long? = null,
    @SerializedName("iou") var iou: Int? = null,
    @SerializedName("iouUnknonwn") var iouUnknonwn: Int? = null,
    @SerializedName("isMask") var isMask: Int? = null,
    @SerializedName("label") var label: String? = null,
    @SerializedName("labelMapWhileSent") var labelMapWhileSent: String? = null,
    @SerializedName("landmarks") var landmarks: ArrayList<Any>? = null,
    @SerializedName("lastFrExecuted") var lastFrExecuted: Int? = null,
    @SerializedName("meanOfBrightness") var meanOfBrightness: Int? = null,
    @SerializedName("min_distance") var minDistance: Double? = null,
    @SerializedName("noseRatio") var noseRatio: Int? = null,
    @SerializedName("pairAlign") var pairAlign: String? = null,
    @SerializedName("pitchDeg") var pitchDeg: Int? = null,
    @SerializedName("ratioDistance") var ratioDistance: Int? = null,
    @SerializedName("recognizedDate") var recognizedDate: RecognizedDateTime? = null,
    @SerializedName("rollDeg") var rollDeg: Int? = null,
    @SerializedName("searchCountWhileSent") var searchCountWhileSent: Int? = null,
    @SerializedName("sharpness") var sharpness: Int? = null,
    @SerializedName("stdOfBrightness") var stdOfBrightness: Int? = null,
    @SerializedName("timeProcessFromInitTrack") var timeProcessFromInitTrack: Int? = null,
    @SerializedName("timeProcessFromInitTrackTotal") var timeProcessFromInitTrackTotal: Int? = null,
    @SerializedName("trackId_WhileSent") var trackIdWhileSent: Int? = null,
    @SerializedName("x1") var x1: Double? = null,
    @SerializedName("x2") var x2: Double? = null,
    @SerializedName("y1") var y1: Double? = null,
    @SerializedName("y2") var y2: Double? = null,
    @SerializedName("yawDeg") var yawDeg: Int? = null
)

data class RecognizedDateTime(
    @SerializedName("date") var date: RecognizedDate? = null,
    @SerializedName("time") var time: RecognizedTime? = null
)

data class RecognizedDate(
    @SerializedName("day") var day: Int? = null,
    @SerializedName("month") var month: Int? = null,
    @SerializedName("year") var year: Int? = null
)

data class RecognizedTime(
    @SerializedName("hour") var hour: Int? = null,
    @SerializedName("minute") var minute: Int? = null,
    @SerializedName("nano") var nano: Int? = null,
    @SerializedName("second") var second: Int? = null
)

data class SearchEventItem(
    @SerializedName("createDate")
    var createDate: Long? = -1,
    @SerializedName("eventDate")
    var eventDate: Long? = -1,
    @SerializedName("crypto")
    var crypto: String? = null,
    @SerializedName("description")
    var description: Description? = null,
    @SerializedName("deviceId")
    var deviceId: String? = null,
    @SerializedName("gatewayId")
    var gatewayId: String? = null,
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("isRead")
    var isRead: Boolean = false,
    @SerializedName("videoUploaded")
    var videoUploaded: Boolean = true,
    @SerializedName("lastModified")
    var lastModified: Long? = null,
    @SerializedName("stringDescription")
    var stringDescription: String? = null,
    @SerializedName("thumbnail")
    var thumbnail: Thumbnail? = null,
    @SerializedName("type")
    var type: Int = 0,
    @SerializedName("videoId")
    var videoId: String? = null,
    var deviceName: String? = "",
    var thumbnailUrl: String? = "",
    var isChecked: Boolean = false,
    var isDeleted: Boolean = false,
    var fileName: String = ""
) : Serializable

data class Thumbnail(
    @SerializedName("bucketName")
    var bucketName: String? = null,
    @SerializedName("fileChecksum")
    var fileChecksum: String? = null,
    @SerializedName("fileExtension")
    var fileExtension: String? = null,
    @SerializedName("fileName")
    var fileName: String? = null,
    @SerializedName("fileSize")
    var fileSize: Long = 0,
    @SerializedName("fileURL")
    var fileURL: String? = null,
    @SerializedName("fileURLExpirationDate")
    var fileURLExpirationDate: String? = null,
    @SerializedName("fileURLExpired")
    var fileURLExpired: Boolean = false,
    @SerializedName("status")
    var status: Int = 0,
)

