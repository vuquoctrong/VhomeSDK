package com.viettel.vht.sdk.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.media.Image.Plane
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.utils.MacroUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer


object ImageUtils {

    const val MAX_WIDTH = 1920
    const val MAX_HEIGHT = 1080


    fun toBitmap(image: Image?): Bitmap? {
        if (image == null) return null
        val yBuffer = image.planes[0].buffer // Y
        val vuBuffer = image.planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }



    private fun rotateBitmap(
        bitmap: Bitmap, rotationDegrees: Int, flipX: Boolean, flipY: Boolean
    ): Bitmap? {
        val matrix = Matrix()

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees.toFloat())

        // Mirror the image along the X or Y axis.
        matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }

    private fun yuv420ThreePlanesToNV21(
        yuv420888planes: Array<Plane>, width: Int, height: Int
    ): ByteBuffer? {
        val imageSize = width * height
        val out = ByteArray(imageSize + 2 * (imageSize / 4))
        if (areUVPlanesNV21(yuv420888planes, width, height)) {
            // Copy the Y values.
            yuv420888planes[0].buffer[out, 0, imageSize]
            val uBuffer = yuv420888planes[1].buffer
            val vBuffer = yuv420888planes[2].buffer
            // Get the first V value from the V buffer, since the U buffer does not contain it.
            vBuffer[out, imageSize, 1]
            // Copy the first U value and the remaining VU values from the U buffer.
            uBuffer[out, imageSize + 1, 2 * imageSize / 4 - 1]
        } else {
            // Fallback to copying the UV values one by one, which is slower but also works.
            // Unpack Y.
            unpackPlane(yuv420888planes[0], width, height, out, 0, 1)
            // Unpack U.
            unpackPlane(yuv420888planes[1], width, height, out, imageSize + 1, 2)
            // Unpack V.
            unpackPlane(yuv420888planes[2], width, height, out, imageSize, 2)
        }
        return ByteBuffer.wrap(out)
    }

    private fun unpackPlane(
        plane: Plane, width: Int, height: Int, out: ByteArray, offset: Int, pixelStride: Int
    ) {
        val buffer = plane.buffer
        buffer.rewind()

        // Compute the size of the current plane.
        // We assume that it has the aspect ratio as the original image.
        val numRow = (buffer.limit() + plane.rowStride - 1) / plane.rowStride
        if (numRow == 0) {
            return
        }
        val scaleFactor = height / numRow
        val numCol = width / scaleFactor

        // Extract the data in the output buffer.
        var outputPos = offset
        var rowStart = 0
        for (row in 0 until numRow) {
            var inputPos = rowStart
            for (col in 0 until numCol) {
                out[outputPos] = buffer[inputPos]
                outputPos += pixelStride
                inputPos += plane.pixelStride
            }
            rowStart += plane.rowStride
        }
    }

    /** Checks if the UV plane buffers of a YUV_420_888 image are in the NV21 format.  */
    private fun areUVPlanesNV21(planes: Array<Plane>, width: Int, height: Int): Boolean {
        val imageSize = width * height
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        // Backup buffer properties.
        val vBufferPosition = vBuffer.position()
        val uBufferLimit = uBuffer.limit()

        // Advance the V buffer by 1 byte, since the U buffer will not contain the first V value.
        vBuffer.position(vBufferPosition + 1)
        // Chop off the last byte of the U buffer, since the V buffer will not contain the last U value.
        uBuffer.limit(uBufferLimit - 1)

        // Check that the buffers are equal and have the expected number of elements.
        val areNV21 =
            vBuffer.remaining() == 2 * imageSize / 4 - 2 && vBuffer.compareTo(uBuffer) == 0

        // Restore buffers to their initial state.
        vBuffer.position(vBufferPosition)
        uBuffer.limit(uBufferLimit)
        return areNV21
    }

    fun scaleBitmap(bitmap: Bitmap): Bitmap {
        val isPortrait = bitmap.isPortrait()
        if ((isPortrait && bitmap.width <= MAX_HEIGHT && bitmap.height <= MAX_WIDTH) ||
            (!isPortrait && bitmap.width <= MAX_WIDTH && bitmap.height <= MAX_HEIGHT)
        ) {
            return bitmap
        }
        val width: Int = bitmap.width
        val height: Int = bitmap.height
        val scaleWidth =
            if (isPortrait) MAX_HEIGHT.toFloat() / width else MAX_WIDTH.toFloat() / width
        val scaleHeight =
            if (isPortrait) MAX_WIDTH.toFloat() / height else MAX_HEIGHT.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        val resizedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, width, height, matrix, false
        )
        bitmap.recycle()
        return resizedBitmap
    }

    private fun Bitmap.isPortrait(): Boolean {
        return width < height
    }

    fun getScreenshotFile(deviceID: String, context: Context): File {
        val folder =
            context.getExternalFilesDir(null).toString() + "screenshot"
        val dir = File(folder)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, "$deviceID.jpeg")
    }

    fun String.getBitmapFromFilePath(): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            bitmap = BitmapFactory.decodeFile(this, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    fun saveImageToGalleryJFCamera(context: Context, path: String, devId: String) {
        val fileName =
            File(path).name.toString().removeRange(14, File(path).name.toString().length)
        val fileNameRename = "${MacroUtils.getValue(context,"SDK_VHOME_APP_NAME")}_${devId}_$fileName.jpg"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis() / 1000)
            values.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "DCIM/${MacroUtils.getValue(context,"SDK_VHOME_APP_NAME")}"
            )
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileNameRename)

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
            } else {
                throw Exception()
            }
        } else {
            val directory =
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        .toString(), MacroUtils.getValue(context,"SDK_VHOME_APP_NAME")
                )
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, fileNameRename)
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

    fun saveVideoToGalleryJFCamera(context: Context, path: String, devId: String) {
        val fileName = "${MacroUtils.getValue(context,"SDK_VHOME_APP_NAME")}_${devId}_${File(path).name}"
        val dateAdded = System.currentTimeMillis()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            values.put(MediaStore.Video.Media.DATE_ADDED, dateAdded)
            values.put(MediaStore.Video.Media.DATE_TAKEN, dateAdded)
            values.put(
                MediaStore.Video.Media.RELATIVE_PATH,
                "DCIM/${MacroUtils.getValue(context,"SDK_VHOME_APP_NAME")}"
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
                    MacroUtils.getValue(context,"SDK_VHOME_APP_NAME")
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

}