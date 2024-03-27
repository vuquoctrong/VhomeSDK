package com.viettel.vht.sdk.module

import android.content.Context
import com.vht.sdkcore.pref.RxPreferences
import com.viettel.vht.sdk.funtionsdk.VHomeSDKManager
import com.viettel.vht.sdk.funtionsdk.VHomeSDKManagerImpl
import com.viettel.vht.sdk.network.ApiInterface
import com.viettel.vht.sdk.network.AuthApiInterface
import com.viettel.vht.sdk.network.NetworkEvent
import com.viettel.vht.sdk.network.RefreshTokenInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FunctionSDKVHomeModule {
    @Provides
    @Singleton
    fun  provideVHomeSDKManager(
        @ApplicationContext context: Context,
        coroutineScope: CoroutineScope,
        rxPreferences: RxPreferences,
        authApiInterface: AuthApiInterface,
        apiInterface: ApiInterface,
        apiRefreshTokenInterface: RefreshTokenInterface,
        networkEvent: NetworkEvent
    ): VHomeSDKManager = VHomeSDKManagerImpl(context, coroutineScope,rxPreferences,authApiInterface,apiInterface,apiRefreshTokenInterface,networkEvent)
}