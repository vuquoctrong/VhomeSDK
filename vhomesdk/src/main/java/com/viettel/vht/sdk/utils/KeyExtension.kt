package com.viettel.vht.sdk.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*


@Synchronized
fun concat(vararg arrays: ByteArray): ByteArray {
    // Determine the length of the result array
    var totalLength = 0
    for (i in arrays.indices) {
        totalLength += arrays[i].size
    }

    // create the result array
    val result = ByteArray(totalLength)

    // copy the source arrays into the result array
    var currentIndex = 0
    for (i in arrays.indices) {
        System.arraycopy(arrays[i], 0, result, currentIndex, arrays[i].size)
        currentIndex += arrays[i].size
    }
    return result
}


fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun File.isNotEmpty(): Boolean {
    return this.exists() && this.length() != 0L
}

fun String.readFirstLine(filePath: String): String? {
    val reader = BufferedReader(FileReader(filePath))
    return reader.readLine()
}



fun String.getBitmapFromFilePath(): Bitmap? {
    var bitmap: Bitmap? = null
    try {
//        val f = File(this)
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        bitmap = BitmapFactory.decodeFile(this, options)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return bitmap
}


fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

fun EditText.onTextChanged(onTextChanged: (Int) ->Unit){
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged.invoke(count)
        }

        override fun afterTextChanged(editable: Editable?) {
        }
    })
}

fun Long.getHour(timeZone: String): Int {
    val c = Calendar.getInstance()
    //  c.timeZone = TimeZone.getTimeZone(timeZone)
    c.timeInMillis = this
    return c.get(Calendar.HOUR_OF_DAY)
}

fun Int.convert24Hours(): String {
    val data = (this / 3600)
    return if (data < 10) {
        "0$data"
    } else {
        data.toString()
    }
}

fun Int.convertHourToSecond(): Int {
    return this * 60 * 60
}

fun Int.convertMinuteToSecond(): Int {
    return this * 60
}

fun Int.convert60Minutes(totalMilisecond: Int, hour: Int): String {
    val minute = ((totalMilisecond - hour * 3600) / 60)
    return if (minute < 10) "0$minute" else minute.toString()
}

fun Long.getMinute(timeZone: String): Int {
    val c = Calendar.getInstance()
    // c.timeZone = TimeZone.getTimeZone(timeZone)
    c.timeInMillis = this
    return c.get(Calendar.MINUTE)
}

fun Long.convertMinuteOffDay(): Long {
    val c = Calendar.getInstance()
    // c.timeZone = TimeZone.getTimeZone(timeZone)
    c.timeInMillis = this
    val result = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
    return result.toLong()
}

fun Int.convertHourAndMinuteToLong(minute: Int, timeZone: String): Long {
    val c = Calendar.getInstance()
    //c.timeZone = TimeZone.getTimeZone(timeZone)
    c.set(Calendar.HOUR_OF_DAY, this)
    c.set(Calendar.MINUTE, minute)
    return c.timeInMillis
}

fun Int.convertStringTime(): String {
    return if (this < 10) {
        "0$this"
    } else {
        "$this"
    }
}

@Throws(IOException::class)
fun File.copyFileAndroid11(destFile: File) {
    if (!destFile.parentFile.exists()) destFile.parentFile.mkdirs()
    if (!destFile.exists()) {
        destFile.createNewFile()
    }
    var source: FileChannel? = null
    var destination: FileChannel? = null
    try {
        source = FileInputStream(this).channel
        destination = FileOutputStream(destFile).channel
        destination.transferFrom(source, 0, source.size())
    } finally {
        source?.close()
        destination?.close()
    }
}

fun bytesToHex(hash: ByteArray): String? {
    val hexString = StringBuilder(2 * hash.size)
    for (i in hash.indices) {
        val hex = Integer.toHexString(0xff and hash[i].toInt())
        if (hex.length == 1) {
            hexString.append('0')
        }
        hexString.append(hex)
    }
    return hexString.toString()
}




