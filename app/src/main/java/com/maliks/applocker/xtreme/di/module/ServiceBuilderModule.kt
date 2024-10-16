package com.maliks.applocker.xtreme.di.module

import com.maliks.applocker.xtreme.service.AppLockerService
import com.maliks.applocker.xtreme.ui.callblocker.service.CallBlockerScreeningService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    abstract fun appLockerService(): AppLockerService

    @ContributesAndroidInjector
    abstract fun callBlockerService(): CallBlockerScreeningService
}