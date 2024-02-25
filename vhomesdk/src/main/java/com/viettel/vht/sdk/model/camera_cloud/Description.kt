package com.viettel.vht.sdk.model.camera_cloud

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Description(
    val en: String = "",
    val enRaw: String = "",
    val vi: String = "",
    val viRaw: String = ""
): Parcelable