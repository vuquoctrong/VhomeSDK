package com.vht.sdkcore.module

import android.content.Context
import com.vht.sdkcore.network.connectivity.NetworkConnectionManager
import com.vht.sdkcore.network.connectivity.NetworkConnectionManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkMonitorModule {

    @Provides
    @Singleton
    fun provideCoroutineScope() =
        CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Provides
    @Singleton
    fun bindNetworkConnectionManager(
        @ApplicationContext context: Context,
        coroutineScope: CoroutineScope
    ): NetworkConnectionManager = NetworkConnectionManagerImpl(context, coroutineScope)
}