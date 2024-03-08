package com.vht.sdkcore.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import com.vht.sdkcore.utils.Constants.PHOTO_FOLDER
import timber.log.Timber
import java.io.*
import java.net.Inet4Address
import java.net.NetworkInterface
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class Utils {

    companion object {

        private val FIVE_MINUTE = 1000 * 60 * 5
        const val VERSION_INDEX = 3
        /// @param folderName can be your app's name
//        fun saveImage(bitmap: Bitmap, context: Context, folderName: String) {
//            val directory = File("${Environment.getExternalStorageDirectory()}/$folderName")
//
//            if (!directory.exists()) {
//                directory.mkdirs()
//            }
//            val fileName = System.currentTimeMillis().toString() + ".png"
//            val file = File(directory, fileName)
//            try {
//                saveImageToStream(bitmap, FileOutputStream(file))
//                val values = ContentValues()
//                values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
//                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
//                if(Build.VERSION.SDK_INT >= 29) {
//                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
//                } else {
//                    // .DATA is deprecated in API 29
//                    values.put(MediaStore.Images.Media.DATA, file.absolutePath)
//                }
//                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//            } catch (e: FileNotFoundException) {
//                Timber.d("FileNotFound")
//            }
//        }

        private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
            if (outputStream != null) {
                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

//        fun getRecordVideoPath(context: Context, folderName: String, nameCamera: String): String {
//            val fileName = nameCamera.setNameFileSave(".mp4")
//            // getExternalStorageDirectory is deprecated in API 29
//            val directory =
//                File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/$folderName")
//
//            if (!directory.exists()) {
//                directory.mkdirs()
//            }
//            val file = File(directory, fileName)
//            try {
//                val values = ContentValues()
//                values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
//                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
//                if (Build.VERSION.SDK_INT >= 29) {
//                    values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
//                } else {
//                    // .DATA is deprecated in API 29
//                    values.put(MediaStore.Video.Media.DATA, file.absolutePath)
//                }
//                context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
//            } catch (e: FileNotFoundException) {
//                Timber.d("FileNotFound")
//            }
//            return file.path
//        }

        fun String.setNameFileSave(type: String): String {
            val date = Calendar.getInstance()
            val year = date.get(Calendar.YEAR)
            val monthOfYear = date.get(Calendar.MONTH) + 1
            val dayOfMonth = date.get(Calendar.DAY_OF_MONTH)
            val hour = date.get(Calendar.HOUR_OF_DAY)
            val minute = date.get(Calendar.MINUTE)
            var second = date.get(Calendar.SECOND)
            var name = this.trim().replace(" ", "_")
            return "VTT_$name" + "_$year$monthOfYear$dayOfMonth$hour$minute$second" + type
        }

        fun getRecordVideoPath(context: Context, folderName: String, nameCamera: String): String {
            val fileName = nameCamera.setNameFileSave(".mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                var dir: File? = null
                dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/$folderName")
                } else {
                    File(Environment.getExternalStorageDirectory().toString() + "/$folderName")
                }

                // Make sure the path directory exists.

                // Make sure the path directory exists.
                if (!dir.exists()) {
                    // Make it, if it doesn't exit
                    val success = dir.mkdirs()
                    if (!success) {
                        dir = null
                    }
                }
                val file = File(dir, fileName)
                try {
                    val values = ContentValues()
                    values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    values.put(MediaStore.Video.Media.DATA, file.absolutePath)
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                    context.contentResolver.insert(
                        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                        values
                    )
                } catch (e: FileNotFoundException) {
                    // Timber.d("FileNotFound")
                }
                return file.path
            } else {
                val directory =
                    File(Environment.getExternalStorageDirectory().toString() + "/$folderName")
                // getExternalStorageDirectory is deprecated in API 29

                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val file = File(directory, fileName)
                try {
                    val values = ContentValues()
                    values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    values.put(MediaStore.Video.Media.DATA, file.absolutePath)
                    // .DATA is deprecated in API 29
                    context.contentResolver.insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                } catch (e: FileNotFoundException) {
                    // Timber.d("FileNotFound")
                }
                return file.path
            }
        }

        fun getRecordVideoPath(context: Context, nameCamera: String): String {
            val folder = File(context.filesDir, "${Constants.VIDEO_FOLDER}/${nameCamera}")
            if (!folder.exists()) folder.mkdirs()
            val date = Calendar.getInstance()
            val year = date.get(Calendar.YEAR)
            val monthOfYear = date.get(Calendar.MONTH) + 1
            val dayOfMonth = date.get(Calendar.DAY_OF_MONTH)
            val hour = date.get(Calendar.HOUR_OF_DAY)
            val minute = date.get(Calendar.MINUTE)
            var second = date.get(Calendar.SECOND)
            val time = "_$year$monthOfYear$dayOfMonth$hour$minute$second"
            val fileName = "Vhome_${nameCamera}_$time.mp4"
            val fileSave = File(folder, fileName)
            return fileSave.path
        }

        fun getRecordVideoPathAvi(context: Context, nameCamera: String): String {
            val name = nameCamera.trim().replace(" ", "_")
            val folder = File(context.filesDir, "${Constants.VIDEO_FOLDER_AVI}/${name}")
            if (!folder.exists()) folder.mkdirs()
            val date = Calendar.getInstance()
            val year = date.get(Calendar.YEAR)
            val monthOfYear = date.get(Calendar.MONTH) + 1
            val dayOfMonth = date.get(Calendar.DAY_OF_MONTH)
            val hour = date.get(Calendar.HOUR_OF_DAY)
            val minute = date.get(Calendar.MINUTE)
            val second = date.get(Calendar.SECOND)
            val time = "_$year$monthOfYear$dayOfMonth$hour$minute$second"
            val fileName = "Vhome_${name}_$time.mp4"
            val fileSave = File(folder, fileName)
            return fileSave.path
        }

        fun saveVideoToGallery(context: Context, path: String,appName: String? = "Vhome") {
            val fileName = File(path).name
            val dateAdded = System.currentTimeMillis()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues()
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                values.put(MediaStore.Video.Media.DATE_ADDED, dateAdded)
                values.put(MediaStore.Video.Media.DATE_TAKEN, dateAdded)
                values.put(
                    MediaStore.Video.Media.RELATIVE_PATH,
                    "DCIM/${appName}"
                )
                values.put(MediaStore.Video.Media.IS_PENDING, true)
                values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName)

                val uri: Uri? =
                    context.contentResolver.insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)
                        ?.write(File(path).inputStream().readBytes())
                    values.put(MediaStore.Video.Media.IS_PENDING, false)
                    context.contentResolver.update(uri, values, null, null)
                }
            } else {
                val directory =
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        appName
                    )
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val file = File(directory, fileName)
                file.outputStream().write(File(path).readBytes())
                val values = ContentValues()
                values.put(MediaStore.Video.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            }
        }

        fun downloadEventToStorage(context: Context, nameCamera: String, inputStream: InputStream) {
            val date = Calendar.getInstance()
            val year = date.get(Calendar.YEAR)
            val monthOfYear = date.get(Calendar.MONTH) + 1
            val dayOfMonth = date.get(Calendar.DAY_OF_MONTH)
            val hour = date.get(Calendar.HOUR_OF_DAY)
            val minute = date.get(Calendar.MINUTE)
            var second = date.get(Calendar.SECOND)
            val time = "_$year$monthOfYear$dayOfMonth$hour$minute$second"
            val fileName = "Vhome_${nameCamera}_$time.mp4"
            val dateAdded = fileName.substringAfterLast("_").substringBefore(".").toLong() / 1000
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues()
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                values.put(MediaStore.Video.Media.DATE_ADDED, dateAdded)
                values.put(MediaStore.Video.Media.DATE_TAKEN, dateAdded)
                values.put(
                    MediaStore.Video.Media.RELATIVE_PATH,
                    "DCIM/${Constants.VIDEO_FOLDER}"
                )
                values.put(MediaStore.Video.Media.IS_PENDING, true)
                values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName)

                val uri: Uri? =
                    context.contentResolver.insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)
                        ?.write(inputStream.readBytes())
                    values.put(MediaStore.Video.Media.IS_PENDING, false)
                    context.contentResolver.update(uri, values, null, null)
                }
            } else {
                val directory =
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                            .toString() + Constants.VIDEO_FOLDER
                    )
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val file = File(directory, fileName)
                file.outputStream().write(inputStream.readBytes())
                val values = ContentValues()
                values.put(MediaStore.Video.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            }
        }


        fun saveVideoEvent(context: Context, path: String) {
            val fileName = "Event_" + System.currentTimeMillis().toString() + ".mp4"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues()
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                values.put(
                    MediaStore.Video.Media.RELATIVE_PATH,
                    "DCIM/${Constants.VIDEO_FOLDER}"
                )
                values.put(MediaStore.Video.Media.IS_PENDING, true)
                values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName)

                val uri: Uri? =
                    context.contentResolver.insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)
                        ?.write(File(path).inputStream().readBytes())
                    values.put(MediaStore.Video.Media.IS_PENDING, false)
                    context.contentResolver.update(uri, values, null, null)
                }
            } else {
                val directory =
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                            .toString() + Constants.VIDEO_FOLDER
                    )
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val file = File(directory, fileName)
                file.outputStream().write(File(path).readBytes())
                val values = ContentValues()
                values.put(MediaStore.Video.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            }
        }

        fun saveVideoPathCameraIPC(
            context: Context,
            folderName: String = Constants.VIDEO_FOLDER,
            nameCamera: String
        ): String {
            val fileName = "VTHome_${nameCamera}_${System.currentTimeMillis()}.mp4"
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                var dir: File? = null
                dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                            .toString() + "/$folderName"
                    )
                } else {
                    File(
                        Environment.getExternalStorageDirectory()
                            .toString() + "/$folderName"
                    )
                }

                // Make sure the path directory exists.

                // Make sure the path directory exists.
                if (!dir.exists()) {
                    // Make it, if it doesn't exit
                    val success = dir.mkdirs()
                    if (!success) {
                        dir = null
                    }
                }
                val file = File(dir, fileName)
                try {
                    val values = ContentValues()
                    values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    values.put(MediaStore.Video.Media.DATA, file.absolutePath)
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                    context.contentResolver.insert(
                        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                        values
                    )
                } catch (e: FileNotFoundException) {
                    // Timber.d("FileNotFound")
                }
                return file.path
            } else {
                val directory = File(
                    Environment.getExternalStorageDirectory()
                        .toString() + "/$folderName"
                )
                // getExternalStorageDirectory is deprecated in API 29

                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val file = File(directory, fileName)
                try {
                    val values = ContentValues()
                    values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    values.put(MediaStore.Video.Media.DATA, file.absolutePath)
                    // .DATA is deprecated in API 29
                    context.contentResolver.insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                } catch (e: FileNotFoundException) {
                    // Timber.d("FileNotFound")
                }
                return file.path
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun saveImage(
            bitmap: Bitmap,
            context: Context,
            nameCamera: String? = null,
            folderName: String
        ) {
            val fileName = nameCamera?.setNameFileSave(".jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                values.put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "DCIM/VHome/Photo/${folderName}"
                )
                values.put(MediaStore.Images.Media.IS_PENDING, true)
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)

                val uri: Uri? =
                    context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                if (uri != null) {
                    saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    context.contentResolver.update(uri, values, null, null)
                }
            } else {
                val directory =
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                            .toString() + "VHome/Photo/${folderName}"
                    )
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val file = File(directory, fileName)
                saveImageToStream(bitmap, FileOutputStream(file))
                if (file.absolutePath != null) {
                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                    // .DATA is deprecated in API 29
                    context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                }
            }
        }

        fun getFolderImage(): File {
            return File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString(), Constants.PHOTO_FOLDER
            )
        }

        fun File.isVideo(): Boolean {
            return path.endsWith(".mp4") || path.endsWith(".3gp")
        }

        fun File.isImage(): Boolean {
            return path.endsWith(".jpg") ||
                    path.endsWith(".png") ||
                    path.endsWith(".gif") ||
                    path.endsWith(".jpeg")
        }


        fun getFileGallery(context: Context): List<String> {
            val imageFolder = File(context.filesDir, PHOTO_FOLDER)
            val listPath = mutableListOf<String>()
            listPath.addAll(getListPath(imageFolder))
            return listPath
        }

        fun getListPath(file: File): List<String> {
            val listPath = mutableListOf<String>()
            if (file.isDirectory) {
                val listFolder = file.listFiles()
                listFolder.forEach {
                    val listChild = getListPath(it)
                    listPath.addAll(listChild)
                }
            } else {
                listPath.add(file.path)
            }
            return listPath
        }

        fun saveImageToGallery(context: Context, path: String,appName: String) {
            val fileName = File(path).name
//            val dateAdded = fileName.substringAfterLast("_").substringBefore(".").toLong() / 1000
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//                values.put(MediaStore.Images.Media.DATE_ADDED, dateAdded)
//                values.put(MediaStore.Images.Media.DATE_TAKEN, dateAdded)
                values.put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "DCIM/${appName}"
                )
                values.put(MediaStore.Images.Media.IS_PENDING, true)
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)

                val uri: Uri? =
                    context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)
                        ?.write(File(path).inputStream().readBytes())
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    context.contentResolver.update(uri, values, null, null)
                }
            } else {
                val directory =
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                            .toString(), appName
                    )
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val file = File(directory, fileName)
                file.outputStream().write(File(path).readBytes())
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            }
        }




        fun getImagePathInInternalStorage(context: Context, serialNumber: String): String {
            val folder = File(context.filesDir, "${Constants.PHOTO_FOLDER}/${serialNumber}")
            if (!folder.exists()) folder.mkdirs()
            val date = Calendar.getInstance()
            val year = date.get(Calendar.YEAR)
            val monthOfYear = date.get(Calendar.MONTH) + 1
            val dayOfMonth = date.get(Calendar.DAY_OF_MONTH)
            val hour = date.get(Calendar.HOUR_OF_DAY)
            val minute = date.get(Calendar.MINUTE)
            val second = date.get(Calendar.SECOND)
            val time = "$year$monthOfYear$dayOfMonth$hour$minute$second"
            val fileName = "Vhome_${serialNumber}_$time.jpeg"
            val fileSave = File(folder, fileName)
            return fileSave.path
        }

        fun saveImageToInternalStorage(
            bitmap: Bitmap,
            context: Context,
            nameCamera: String? = null
        ): String {
            val folder = File(context.filesDir, "${Constants.PHOTO_FOLDER}/${nameCamera}")
            if (!folder.exists()) folder.mkdirs()
            val date = Calendar.getInstance()
            val year = date.get(Calendar.YEAR)
            val monthOfYear = date.get(Calendar.MONTH) + 1
            val dayOfMonth = date.get(Calendar.DAY_OF_MONTH)
            val hour = date.get(Calendar.HOUR_OF_DAY)
            val minute = date.get(Calendar.MINUTE)
            val second = date.get(Calendar.SECOND)
            val time = "$year$monthOfYear$dayOfMonth$hour$minute$second"
            val fileName = "Vhome_${nameCamera}_$time.jpeg"

            val fileSave = File(folder, fileName)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileSave.outputStream())

            return fileSave.path
        }

        fun imageInternalStoragePath(context: Context, cameraName: String): String {
            val folder = File(context.filesDir, "${Constants.PHOTO_FOLDER}/${cameraName}")
            if (!folder.exists()) folder.mkdirs()
            val date = Calendar.getInstance()
            val year = date.get(Calendar.YEAR)
            val monthOfYear = date.get(Calendar.MONTH) + 1
            val dayOfMonth = date.get(Calendar.DAY_OF_MONTH)
            val hour = date.get(Calendar.HOUR_OF_DAY)
            val minute = date.get(Calendar.MINUTE)
            val second = date.get(Calendar.SECOND)
            val time = "$year$monthOfYear$dayOfMonth$hour$minute$second"
            val fileName = "Vhome_${cameraName}_$time.jpeg"

            return File(folder, fileName).path
        }

        fun folderImageInternalStoragePath(context: Context): String {
            val folder = File(context.filesDir, Constants.PHOTO_FOLDER)
            if (!folder.exists()) folder.mkdirs()
            return folder.path
        }

        fun folderThumbnailInternalStoragePath(context: Context): String {
            val folder = File(context.filesDir, Constants.CACHE_THUMBNAIL)
            if (!folder.exists()) folder.mkdirs()
            return folder.path
        }

        fun videoInternalStoragePath(context: Context, nameCamera: String): String {
            val folder = File(context.filesDir, "${Constants.VIDEO_FOLDER}/${nameCamera}")
            if (!folder.exists()) folder.mkdirs()
            val date = Calendar.getInstance()
            val year = date.get(Calendar.YEAR)
            val monthOfYear = date.get(Calendar.MONTH) + 1
            val dayOfMonth = date.get(Calendar.DAY_OF_MONTH)
            val hour = date.get(Calendar.HOUR_OF_DAY)
            val minute = date.get(Calendar.MINUTE)
            val second = date.get(Calendar.SECOND)
            val time = "_$year$monthOfYear$dayOfMonth$hour$minute$second"
            val fileName = "Vhome_${nameCamera}_$time.mp4"
            return File(folder, fileName).path
        }

        fun getIpv4Address(): String? {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val interfaceObj = interfaces.nextElement()
                    val addresses = interfaceObj.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (!address.isLoopbackAddress && address is Inet4Address) {
                            return address.hostAddress
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e("getIpv4Address ${e.printStackTrace()}")
            }
            return Constants.EMPTY
        }

        fun formatQualityWifiSignal(size: Long): Double {
            val df = DecimalFormat("#.#", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
            val wrongSize = 0.0
            return if (size == 0L) {
                wrongSize
            } else {
                df.format(size.toDouble() / 1048576.0).toDouble()
            }
        }

        fun deleteFile(fileStr: String): Boolean {
            return if (!TextUtils.isEmpty(fileStr)) {
                val file = File(fileStr)
                if (file.exists()) {
                    file.delete()
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    fun formatBitrate(size: Long): String? {
        val df = DecimalFormat("#.0", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
        var fileSizeString = ""
        val wrongSize = "0 KB/s"
        return if (size == 0L) {
            wrongSize
        } else {
            fileSizeString = if (size < 1024L) {
                df.format(size.toDouble()) + " B/s"
            } else if (size < 1048576L) {
                df.format(size.toDouble() / 1024.0) + " KB/s"
            } else if (size < 1073741824L) {
                df.format(size.toDouble() / 1048576.0) + " MB/s"
            } else {
                df.format(size.toDouble() / 1.073741824E9) + " GB/s"
            }
            fileSizeString
        }
    }

}