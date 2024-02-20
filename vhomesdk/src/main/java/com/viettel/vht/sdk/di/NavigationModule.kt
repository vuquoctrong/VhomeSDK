package com.viettel.vht.sdk.di

import com.vht.sdkcore.navigationComponent.BaseNavigator
import com.viettel.vht.sdk.navigation.AppNavigatorImpl
import com.viettel.vht.sdk.navigation.AppNavigation
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class NavigationModule {

    @Binds
    abstract fun provideBaseNavigation(navigation: AppNavigatorImpl): BaseNavigator

    @Binds
    abstract fun provideAppNavigation(navigation: AppNavigatorImpl): AppNavigation

}