@file:Suppress("UNCHECKED_CAST")

package com.viettel.vht.sdk.utils

//import com.tuya.smart.sdk.bean.DeviceBean
//import com.tuya.smart.sdk.bean.DeviceBean
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.viettel.vht.sdk.model.DeviceDataResponse
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

fun List<DeviceDataResponse>?.replace(device: DeviceDataResponse) {
    val index = this?.indexOfFirst { it.id == device.id } ?: -1
    if (index != -1) this?.toMutableList()?.set(index, device)
}


fun List<DeviceDataResponse>.randomCamera(): DeviceDataResponse? {
    forEach { if (it.isCamera()) return it }
    return null
}

fun View.setSingleClick(delay: Long = 500L, action: (View) -> Unit) {
    setOnClickListener(object : View.OnClickListener {
        private var lastClick = 0L

        override fun onClick(v: View?) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClick > delay) {
                lastClick = currentTime
                action.invoke(this@setSingleClick)
            }
        }
    })
}

fun <T : java.io.Serializable> Fragment.getArgSerializable(key: String, classOfT: Class<T>): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        requireArguments().getSerializable(key, classOfT)!!
    } else {
        requireArguments().getSerializable(key) as T
    }
}



fun Bitmap.toTempUri(context: Context): Pair<String, Uri> {
    val storageDir = File(context.filesDir.path + "/FaceImages")
    if (!storageDir.exists()) {
        storageDir.mkdirs()
    }
    val tmp = File.createTempFile(
        "IMG${Calendar.getInstance().timeInMillis}",  // prefix
        ".jpg",         // suffix
        storageDir      // directory
    )
    val outputStream = FileOutputStream(tmp)
    this.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    return Pair(tmp.absolutePath, tmp.toUri())
}

fun File.imageToMediaType(): MediaType {
    val ext = when (this.extension) {
        "jpg", "jpeg" -> "jpeg"
        "png" -> "png"
        else -> ""
    }
    return "image/$ext".toMediaType()
}

fun String.normalizeString(): String {
    val trim = this.trim()
    return trim.replace("\\s+".toRegex(), " ") // Remove redundant spaces
}