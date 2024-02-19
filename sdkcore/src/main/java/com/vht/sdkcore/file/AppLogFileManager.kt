package com.vht.sdkcore.file

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.ActionAppLogModel
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.HTTPAppLogModel
import com.vht.sdkcore.utils.ProtobufAppLogModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogFileManager @Inject constructor(
    private val application: Application,
    @ApplicationContext val context: Context
) {

    @Inject
    lateinit var rxPreferences: RxPreferences

    private lateinit var folder: File
    private lateinit var mFile: File
    private var publicIP: MutableLiveData<String> = MutableLiveData()
    private var packageInfo: PackageInfo? = null

    private  val threadPool by lazy {
        Executors.newSingleThreadExecutor()
    }
    init {
        try {
            packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                application.applicationContext.packageManager.getPackageInfo(
                    application.applicationContext.packageName,
                    PackageManager.PackageInfoFlags.of(0L)
                )
            } else {
                application.applicationContext.packageManager.getPackageInfo(
                    application.applicationContext.packageName,
                    0
                )
            }
        } catch (e: Exception) {

        }
    }

    fun initFolder(context: Context) {
        folder = File(
            context.filesDir,
            Constants.APP_LOG_NAME
        )
        if (!folder.exists()) {
            folder.mkdirs()
        }
    }

    fun initAppLogFile(userId: String) {
        try {
            mFile = File(
                folder,
                "${userId}_${System.currentTimeMillis()}_${System.currentTimeMillis() + FIVE_MINUTES}.txt"
            )
            if (!mFile.exists()) {
                mFile.createNewFile()
            }
        } catch (e: Exception) {
            Log.e(TAG, "initAppLogFile: Error ${e.message}")
        }
    }

    fun saveProtobufAppLogFile(
        protobufAppLogModel: ProtobufAppLogModel,
    ) {
        threadPool.execute {
            try {
                if (mFile.exists()) {
                    // Open a file output stream in "append" mode
                    val fos = FileOutputStream(mFile, true)

                    // Create a writer to write to the file
                    val writer = OutputStreamWriter(fos)

                    // Write the new content to the file
                    val response =
                        if (protobufAppLogModel.tcpResponseTime.isNotEmpty()) "RESPONSE" else ""
                    writer.write(
                        "${protobufAppLogModel.logMode}|${protobufAppLogModel.timeStamp}|${publicIP.value ?: ""}|${protobufAppLogModel.userId}|${protobufAppLogModel.phoneNumber}" +
                                "|${protobufAppLogModel.appAgent}|${packageInfo?.versionName}|${protobufAppLogModel.screenId}|${protobufAppLogModel.actionId}|${protobufAppLogModel.deviceId}|${protobufAppLogModel.serverDomainIP}" +
                                "|${protobufAppLogModel.tcpMethod}|${protobufAppLogModel.tcpURL}|${protobufAppLogModel.tcpVersion}|${protobufAppLogModel.tcpResponseCode}|${protobufAppLogModel.tcpResponseBodyLength}|${protobufAppLogModel.tcpResponseTime}||$response| \n"
                    )

                    // Flush and close the writer and output stream
                    writer.flush()
                    writer.close()
                    fos.close()
                }
            } catch (e: Exception) {
                Log.d(TAG, "saveProtobufAppLogFile: Error ${e.message}")
            }
        }
    }

    fun saveHTTPAppLogFile(
        httpAppLogModel: HTTPAppLogModel
    ) {
        threadPool.execute {
            try {
                if (mFile.exists()) {
                    // Open a file output stream in "append" mode
                    val fos = FileOutputStream(mFile, true)

                    // Create a writer to write to the file
                    val writer = OutputStreamWriter(fos)

                    val response =
                        if (httpAppLogModel.apiResponseCode.isNotEmpty()) "RESPONSE" else ""
                    // Write the new content to the file

                    writer.write(
                        "${httpAppLogModel.logMode}|${httpAppLogModel.timeStamp}|${publicIP.value ?: ""}|${rxPreferences.getUserId()}|${rxPreferences.getUserPhoneNumber()}" +
                                "|${httpAppLogModel.appAgent}|${packageInfo?.versionName}|${httpAppLogModel.screenId}|${httpAppLogModel.actionId}|${httpAppLogModel.deviceId}|${httpAppLogModel.serverDomainIP}" +
                                "|${httpAppLogModel.httpMethod}|${httpAppLogModel.httpURL}|${httpAppLogModel.httpVersion}|${httpAppLogModel.httpResponseCode}|${httpAppLogModel.httpResponseBodyLength}|${httpAppLogModel.httpResponseTime}|${httpAppLogModel.apiResponseCode}|$response|${httpAppLogModel.traceParent}| \n"
                    )
                    // Flush and close the writer and output stream
                    writer.flush()
                    writer.close()
                    fos.close()
                } else {

                }
            } catch (e: Exception) {
                Log.e(TAG, "saveHTTPAppLogFile: Error ${e.message}")
            }
        }

    }

    fun saveActionToAppLogFile(
        actionAppLogModel: ActionAppLogModel
    ) {
        threadPool.execute {
            try {
                if (mFile.exists()) {
                    // Open a file output stream in "append" mode
                    val fos = FileOutputStream(mFile, true)

                    // Create a writer to write to the file
                    val writer = OutputStreamWriter(fos)

                    writer.write(
                        "${actionAppLogModel.logMode}|${System.currentTimeMillis()}|${publicIP.value ?: ""}|${rxPreferences.getUserId()}|${rxPreferences.getUserPhoneNumber()}" +
                                "|${actionAppLogModel.appAgent}|${packageInfo?.versionName}|${actionAppLogModel.screenId}|${actionAppLogModel.actionId}|${actionAppLogModel.deviceId}|${actionAppLogModel.serverDomainIP}" +
                                "|${Constants.EMPTY}|${Constants.EMPTY}|${Constants.EMPTY}|${Constants.EMPTY}|${Constants.EMPTY}|${Constants.EMPTY}|${Constants.EMPTY}|${actionAppLogModel.type}|${Constants.EMPTY}|${actionAppLogModel.loadTime}| \n"
                    )
                    // Flush and close the writer and output stream
                    writer.flush()
                    writer.close()
                    fos.close()
                } else {

                }
            } catch (e: Exception) {
                Log.e(TAG, "saveActionToAppLogFile: Error ${e.message}")
            }
        }

    }

    fun getCurrentFile(): File {
        return mFile
    }

    fun deleteAppLogFile(fileName: String): Boolean {
        try {
            val deleteFile = File(folder, fileName)
            if (deleteFile.exists()) {
                deleteFile.delete()
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteAppLogFile: Error ${e.message}")
        }
        return false
    }

    fun deleteCurrentFile(): Boolean {
        try {
            if (mFile.exists()) {
                mFile.delete()
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteAppLogFile: Error ${e.message}")
        }
        return false
    }

    fun getFolder(): File {
        return folder
    }

    fun deleteContentOfAppLogFile(context: Context) {
        try {
            // Open the file in write mode
            val writer = FileWriter(folder, false)
            // Close the file to delete its contents
            writer.close()
        } catch (e: Exception) {
            Log.e(TAG, "deleteAppLogFile: ${e.message}")
        }
    }

    fun setPublicIP(publicIP: String) {
        this.publicIP.value = publicIP
    }

    companion object {
        private val FIVE_MINUTES = 1000 * 60 * 5
        private const val TAG = "FileManager"
    }

}