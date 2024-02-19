package com.vht.sdkcore.module

import com.vht.sdkcore.database.DatabaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(databaseRepository: DatabaseRepository): DatabaseRepository {
        return databaseRepository
    }
}