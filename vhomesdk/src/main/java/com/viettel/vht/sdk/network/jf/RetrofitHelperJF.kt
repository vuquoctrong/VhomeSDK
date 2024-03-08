package com.viettel.vht.sdk.network.jf

import com.google.gson.GsonBuilder
import com.vht.sdkcore.utils.Constants
import com.viettel.vht.sdk.BuildConfig
import com.viettel.vht.sdk.utils.Config
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitHelperJF {

    fun getInstance(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpBuilder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            httpBuilder.addInterceptor(loggingInterceptor)
        }
        val client: OkHttpClient =
            httpBuilder.readTimeout(Constants.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .connectTimeout(Constants.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .writeTimeout(Constants.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .build()
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder().baseUrl(Config.sdkBASE_URL_CAMERA_JF)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            // we need to add converter factory to
            // convert JSON object to Java object
            .build()
    }
}