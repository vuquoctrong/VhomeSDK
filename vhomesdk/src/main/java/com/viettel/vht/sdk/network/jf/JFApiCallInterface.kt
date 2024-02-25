package com.viettel.vht.sdk.network.jf

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface JFApiCallInterface {

    // check sdt để lấy user_id từ JF
    @POST("usersearch/{version}/{timeMillis}/{secret}.rs")
    suspend fun checkShareJF(
        @Path("version") version: String,
        @Path("timeMillis") timeMillis: String,
        @Path("secret") secret: String,
        @Query("uname") userName: String,
        @Query("upass") userPwd: String,
        @Query("search") searchUserName: String,
        @Header("uuid") uuid: String,
        @Header("appKey") appKey: String,
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    // chia sẻ cam từ JF
    @POST("mdshareadd/{version}/{timeMillis}/{secret}.rs")
    suspend fun requestShareCameraJF(
        @Header("uuid") uuid: String,
        @Header("appKey") appKey: String,
        @Header("Authorization") token: String,
        @Path("version") version: String,
        @Path("timeMillis") timeMillis: String,
        @Path("secret") secret: String,
        @Query("uname")  userName: String,
        @Query("upass")  userPwd:String,
        @Query("shareUuid") shareUuid:String,
        @Query("acceptId") acceptId:String,
        @Query("powers") powers:String,
        @Query("permissions") permissions:String,
    ): Response<ResponseBody>

    // lấy danh sách chia sẻ từ JF
    @POST("mdsharemylist/{version}/{timeMillis}/{secret}.rs")
    suspend fun getListShareCameraJF(
        @Header("uuid") uuid: String,
        @Header("appKey") appKey: String,
        @Header("Authorization") token: String,
        @Path("version") version: String,
        @Path("timeMillis") timeMillis: String,
        @Path("secret") secret: String,
        @Query("uname")  userName: String,
        @Query("upass")  userPwd:String,
        @Query("shareUuid") shareUuid:String
    ): Response<ResponseBody>

    // Xóa chia sẻ JF
    @POST("mdsharedel/{version}/{timeMillis}/{secret}.rs")
    suspend fun cancelShareJF(
        @Header("uuid") uuid: String,
        @Header("appKey") appKey: String,
        @Header("Authorization") token: String,
        @Path("version") version: String,
        @Path("timeMillis") timeMillis: String,
        @Path("secret") secret: String,
        @Query("uname")  userName: String,
        @Query("upass")  userPwd:String,
        @Query("devId") shareUuid:String
    ): Response<ResponseBody>

    // Chấp nhận chia sẻ JF
    @POST("mdshareaccept/{version}/{timeMillis}/{secret}.rs")
    suspend fun acceptShareJF(
        @Header("uuid") uuid: String,
        @Header("appKey") appKey: String,
        @Header("Authorization") token: String,
        @Path("version") version: String,
        @Path("timeMillis") timeMillis: String,
        @Path("secret") secret: String,
        @Query("uname")  userName: String,
        @Query("upass")  userPwd:String,
        @Query("devId") shareUuid:String
    ): Response<ResponseBody>

    // Từ chối chia sẻ JF
    @POST("mdsharerefuse/{version}/{timeMillis}/{secret}.rs")
    suspend fun rejectShareJF(
        @Header("uuid") uuid: String,
        @Header("appKey") appKey: String,
        @Header("Authorization") token: String,
        @Path("version") version: String,
        @Path("timeMillis") timeMillis: String,
        @Path("secret") secret: String,
        @Query("uname")  userName: String,
        @Query("upass")  userPwd:String,
        @Query("devId") shareUuid:String
    ): Response<ResponseBody>

    // lấy danh sách nhan chia sẻ từ JF
    @POST("mdsharelist/{version}/{timeMillis}/{secret}.rs")
    suspend fun getOtherShareDevListJF(
        @Header("uuid") uuid: String,
        @Header("appKey") appKey: String,
        @Header("Authorization") token: String,
        @Path("version") version: String,
        @Path("timeMillis") timeMillis: String,
        @Path("secret") secret: String,
        @Query("uname")  userName: String,
        @Query("upass")  userPwd:String
    ): Response<ResponseBody>

    // update quyen chia se chia sẻ từ JF
    @POST("mdsharesetpermission/{version}/{timeMillis}/{secret}.rs")
    suspend fun setSharePermissionJF(
        @Header("uuid") uuid: String,
        @Header("appKey") appKey: String,
        @Header("Authorization") token: String,
        @Path("version") version: String,
        @Path("timeMillis") timeMillis: String,
        @Path("secret") secret: String,
        @Query("uname")  userName: String,
        @Query("upass")  userPwd:String,
        @Query("shareId") shareId: String,
        @Query("permissions") permission: String
    ): Response<ResponseBody>

    @POST("superPassword/v1/{timeMillis}/{encryptStr}.rs")
    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded")
    suspend fun postPassword(
        @Header("uuid") uuid: String,
        @Header("appKey") appKey: String,
        @Path("encryptStr") encryptStr: String,
        @Path("timeMillis") timeMillis: String,
        @Field("text") body: String
    ): String
}