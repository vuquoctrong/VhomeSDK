package com.viettel.vht.sdk.module

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.utils.MacroUtils
import com.vht.sdkcore.file.AppLogFileManager
import com.viettel.vht.sdk.network.ApiInterface
import com.viettel.vht.sdk.network.AuthApiInterface
import com.viettel.vht.sdk.network.ListRequest
import com.viettel.vht.sdk.network.NetworkEvent
import com.viettel.vht.sdk.network.NetworkInterceptor
import com.viettel.vht.sdk.network.TokenAuthenticator
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Constants
import com.viettel.vht.sdk.BuildConfig
import com.viettel.vht.sdk.utils.Config
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().setLenient().create()
    }

    @Provides
    @Singleton
    fun provideApiInterface(
        gson: Gson,
        @ApplicationContext context: Context,
        @DispatchServerHttpClient client: OkHttpClient
    )
            : ApiInterface {
        val retrofit = Retrofit.Builder()
            .baseUrl(MacroUtils.getValue(context,"SDK_VHOME_BASE_URL"))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        return retrofit.create(ApiInterface::class.java)
    }


    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class DispatchServerHttpClient

    @DispatchServerHttpClient
    @Provides
    @Singleton
    fun provideDispatchServerHttpClient(
        cache: Cache?, rxPreferences: RxPreferences,
        networkInterceptor: NetworkInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder().run {
            cache(cache)
            addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val id = UUID.randomUUID()
                val traceparent =
                    "00-${UUID.randomUUID().toString().replace("-", "")}-0000000000000000-01"
                val request = if (!TextUtils.isEmpty(rxPreferences.getUserToken())) {
                    chain.request()
                        .newBuilder()
                        .header("Content-Type", "application/json")
                        .addHeader(
                            "Authorization", rxPreferences.getUserToken()!!
                        )
                        .addHeader("traceparent", traceparent)
                        .build()
                } else {
                    chain.request()
                        .newBuilder()
                        .header("Content-Type", "application/json")
                        .addHeader("traceparent", traceparent)
                        .build()
                }

                chain.proceed(request)
            })
            if (BuildConfig.DEBUG) {
                addInterceptor(loggingInterceptor)
            }
            addInterceptor(networkInterceptor)
            authenticator(tokenAuthenticator)
            //    addInterceptor(timingInterceptor)
            callTimeout(Constants.DEFAULT_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
            connectTimeout(Constants.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            readTimeout(Constants.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            writeTimeout(Constants.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            build()
        }
    }


    @Provides
    @Singleton
    fun provideAuthApiInterface(
        gson: Gson,
        @ApplicationContext context: Context,
        @AuthServerHttpClient client: OkHttpClient
    ): AuthApiInterface {
        val retrofit = Retrofit.Builder()
            .baseUrl(MacroUtils.getValue(context,"SDK_VHOME_BASE_URL"))
            .client(client)
            .addConverterFactory(NullOnEmptyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        return retrofit.create(AuthApiInterface::class.java)
    }

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class AuthServerHttpClient

    @SuppressLint("HardwareIds")
    @AuthServerHttpClient
    @Provides
    @Singleton
    fun provideAuthServerHttpClient(
        cache: Cache?,
        networkInterceptor: NetworkInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        @ApplicationContext context: Context
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder().run {
            cache(cache)
            addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val traceparent =
                    "00-${UUID.randomUUID().toString().replace("-", "")}-0000000000000000-01"
                val request =
                    chain.request()
                        .newBuilder()
                        .header("Content-Type", "application/json")
                        .addHeader("AppKey", MacroUtils.getValue(context,"SDK_VHOME_APP_KEY"))
                        .addHeader("AppSecret", MacroUtils.getValue(context,"SDK_VHOME_APP_SECKEY"))
                        .addHeader("traceparent", traceparent)
                        .build()

                chain.proceed(request)
            })
            if (BuildConfig.DEBUG) {
                addInterceptor(loggingInterceptor)
            }
            addInterceptor(networkInterceptor)
            authenticator(tokenAuthenticator)
            callTimeout(Constants.DEFAULT_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
            connectTimeout(Constants.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            readTimeout(Constants.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            writeTimeout(Constants.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            build()
        }
    }



    @Provides
    @Singleton
    fun provideCache(@ApplicationContext application: Context): Cache {
        val cacheSize = 10 * 1024 * 1024.toLong() // 10 MB
        val httpCacheDirectory = File(application.cacheDir, "http-cache")
        return Cache(httpCacheDirectory, cacheSize)
    }

    @Provides
    fun provideGsonClient(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @Provides
    @Singleton
    fun providerNetworkEvent() =
        NetworkEvent()

    @Provides
    @Singleton
    fun providerNetworkInterceptor(
        networkEvent: NetworkEvent,
        @ApplicationContext context: Context,
        gson: Gson,
        rxPreferences: RxPreferences,
        appLogFileManager: AppLogFileManager,
        listRequest: ListRequest,
    ) = NetworkInterceptor(
        networkEvent,
        context,
        gson,
        rxPreferences,
        appLogFileManager,
        listRequest
    )

}

class NullOnEmptyConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val delegate: Converter<ResponseBody, Any> =
            retrofit.nextResponseBodyConverter(this, type, annotations)
        return Converter { body -> if (body.contentLength() == 0L) null else delegate.convert(body) }
    }
}