package com.vht.sdkcore.module

import com.vht.sdkcore.pref.AppPreferences
import com.vht.sdkcore.pref.RxPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SharedPrefsModule {
    @Binds
    abstract fun provideRxPreference(preferences: AppPreferences): RxPreferences
}