package com.maliks.applocker.xtreme.di.module

import android.app.Application
import android.content.Context
import com.maliks.applocker.xtreme.AppLockXtremeApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {

    @Provides
    @Singleton
    fun provideContext(AppLockXtremeApplication: AppLockXtremeApplication): Context = AppLockXtremeApplication.applicationContext

    @Provides
    @Singleton
    fun provideApplication(AppLockXtremeApplication: AppLockXtremeApplication): Application = AppLockXtremeApplication
}